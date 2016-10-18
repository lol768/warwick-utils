package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import com.sun.istack.internal.Nullable;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.*;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.strategy.internal.MultipartUploadSlicingAlgorithm;
import org.jclouds.http.HttpResponseException;
import org.jclouds.io.Payload;
import org.jclouds.io.PayloadSlicer;
import org.jclouds.io.internal.BasePayloadSlicer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import uk.ac.warwick.util.concurrency.TaskExecutionCompletionService;
import uk.ac.warwick.util.concurrency.TaskExecutionService;
import uk.ac.warwick.util.files.*;
import uk.ac.warwick.util.files.FileReferenceCreationStrategy.Target;
import uk.ac.warwick.util.files.Storeable.StorageStrategy.MissingContentStrategy;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File store which can ask a FileReferenceCreationStrategy
 */
public final class BlobStoreFileStore implements LocalFileStore, HashFileStore, InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlobStoreFileStore.class);

    private final Map<String, FileHashResolver> hashResolvers;

    private final FileReferenceCreationStrategy storageStrategy;

    private final BlobStore blobStore;

    private final String containerPrefix;

    private final PayloadSlicer payloadSlicer = new BasePayloadSlicer();

    private final TaskExecutionService executionService = new TaskExecutionService(Executors.newCachedThreadPool());

    public BlobStoreFileStore(Map<String,FileHashResolver> resolvers, FileReferenceCreationStrategy strategy, BlobStoreContext context, String containerPrefix) {
        this.hashResolvers = resolvers;
        this.storageStrategy = strategy;
        this.blobStore = context.getBlobStore();
        this.containerPrefix = containerPrefix;
    }

    @Override
    public FileReference store(Storeable storeable, String requestedStoreName, ByteSource in) throws IOException {
        Target target = storageStrategy.select(in);

        switch (target) {
            case local:
                return storeLocalReference(storeable, in);
            case hash:
                return storeHashReference(in, requestedStoreName);
            default:
                throw new IllegalStateException("Unhandled strategy; " + target);
        }
    }

    private FileHashResolver getHashResolver(String hashStoreName) {
        String key = hashStoreName;
        if (key == null) {
            key = FileHashResolver.STORE_NAME_DEFAULT;
        }
        return hashResolvers.get(key);
    }

    private FileHashResolver getHashResolver(HashString hashString) {
        if (hashString.isDefaultStore()) {
            return getHashResolver(FileHashResolver.STORE_NAME_DEFAULT);
        }
        return getHashResolver(hashString.getStoreName());
    }

    @Override
    public HashFileReference storeHashReference(ByteSource in, String requestedHashStore) throws IOException {
        // If there is an existing hash reference, return that; otherwise, store the physical file
        FileHashResolver hashResolver = getHashResolver(requestedHashStore);

        HashString hash;
        try (InputStream is = in.openBufferedStream()) {
            hash = hashResolver.generateHash(is);
        }

        HashFileReference existing = hashResolver.lookupByHash(this, hash, true);
        if (existing.isExists()) {
            return existing;
        }

        return doStore(in, hash, existing);
    }

    @Override
    public HashFileReference createHashReference(ByteSource in, String requestedStoreName) throws IOException {
        return storeHashReference(in, requestedStoreName);
    }
    
    private HashFileReference doStore(ByteSource in, HashString hash, HashFileReference target) throws IOException {
        return doStore(in, hash.getHash(), containerPrefix + (hash.isDefaultStore() ? HashString.DEFAULT_STORE : hash.getStoreName()), target);
    }

    <T extends FileReference> T doStore(ByteSource in, String key, String container, T target) throws IOException {
        if (target.isFileBacked()) {
            throw new UnsupportedOperationException("Storage to file backed data not implemented");
        }

        long size = in.size();
        Blob blob =
            blobStore.blobBuilder(key)
                .payload(in)
                .contentDisposition(key)
                .contentLength(size)
                .build();

        try {
            doPut(container, blob, size);
        } catch (HttpResponseException e) {
            // PHO-247
            LOGGER.warn("PUT: HttpResponseException encountered; might be a 401, so checking for fresh tokens and retrying...");

            // almost any other request will handle a 401 by refreshing the auth token.
            blobStore.blobMetadata(container, key);
            doPut(container, blob, size);
        }

        return target;
    }

    private void doPut(String container, Blob blob, long size) throws IOException {
        // TAB-4144 Use large object support for anything over 50mb
        // TAB-4235 If you want this done in parallel, you have to do it yourself
        if (size > 50 * 1024 * 1024) {
            long partSize =
                new MultipartUploadSlicingAlgorithm(blobStore.getMinimumMultipartPartSize(), blobStore.getMaximumMultipartPartSize(), blobStore.getMaximumNumberOfParts())
                    .calculateChunkSize(size);

            MultipartUpload multipartUpload = blobStore.initiateMultipartUpload(container, blob.getMetadata(), PutOptions.NONE);

            int i = 1;
            TaskExecutionCompletionService<MultipartPart> completionService = executionService.newCompletionService();
            for (Payload payload : payloadSlicer.slice(blob.getPayload(), partSize)) {
                final int index = i;
                completionService.submit(() -> blobStore.uploadMultipartPart(multipartUpload, index, payload));
                i++;
            }

            try {
                // Parts are returned in the order they completed, so we need to sort them into the correct order or the file will be stitched together wrong
                List<MultipartPart> parts =
                    completionService.waitForCompletion(true)
                        .stream()
                        .sorted(Comparator.comparing(MultipartPart::partNumber))
                        .collect(Collectors.toList());

                blobStore.completeMultipartUpload(multipartUpload, parts);
            } catch (InterruptedException | ExecutionException e) {
                throw new IOException(e);
            }
        } else {
            blobStore.putBlob(container, blob, PutOptions.NONE);
        }
    }

    @Override
    public LocalFileReference getForPath(Storeable.StorageStrategy storageStrategy, String path) {
        String container = containerPrefix + storageStrategy.getRootPath();
        
        // If the blob doesn't exist, we still return a file backed reference,
        // and just allow the delegation to isExists() to do its work.
        return new BlobBackedLocalFileReference(this, blobStore, container, path, storageStrategy);
    }

    @Override
    public Stream<String> list(Storeable.StorageStrategy storageStrategy, String basePath) {
        String containerName = containerPrefix + storageStrategy.getRootPath();
        String prefix = basePath + "/";

        PageSet<? extends StorageMetadata> firstResults = blobStore.list(containerName, ListContainerOptions.Builder.prefix(prefix));

        return listKeys(containerName, prefix, firstResults.getNextMarker(), firstResults.stream().map(StorageMetadata::getName));
    }

    private Stream<String> listKeys(String containerName, String prefix, @Nullable String nextMarker, Stream<String> accumulator) {
        if (nextMarker == null) {
            // No more results
            return accumulator;
        } else {
            PageSet<? extends StorageMetadata> nextResults = blobStore.list(containerName, ListContainerOptions.Builder.prefix(prefix).afterMarker(nextMarker));

            return Stream.concat(accumulator, listKeys(containerName, prefix, nextResults.getNextMarker(), nextResults.stream().map(StorageMetadata::getName)));
        }
    }

    private HashFileReference getByFileHash(HashString fileHash) {
        return getHashResolver(fileHash).lookupByHash(this, fileHash, false);
    }

    @Override
    public FileReference get(Storeable storeable) throws FileNotFoundException {
        if (storeable.getHash() != null && !storeable.getHash().isEmpty()) {
            return getByFileHash(storeable.getHash());
        } else {
            FileReference ref;
            LocalFileReference localRef = null;
            if (storeable.getStrategy().isSupportsLocalReferences() || storeable.getStrategy().getMissingContentStrategy() == MissingContentStrategy.Local) {
                localRef = getForPath(storeable.getStrategy(), storeable.getPath());
            }
            
            if (localRef != null && localRef.isExists()) { 
                ref = localRef; 
            } else {
                switch (storeable.getStrategy().getMissingContentStrategy()) {
                    case Local:
                        ref = localRef;
                        break;
                    case Hash:
                        ref = new EmptyHashBackedFileReference(this, storeable.getStrategy().getDefaultHashStore());
                        break;
                    case Exception:
                        throw new FileNotFoundException("Couldn't find a file reference for " + storeable);
                    default:
                        throw new IllegalArgumentException(
                            "Unsupported missing content strategy: " + storeable.getStrategy().getMissingContentStrategy()
                        );
                }
            }
            
            return ref;
        }
    }

    @Override
    public LocalFileReference storeLocalReference(Storeable storeable, ByteSource in) throws IOException {
        LocalFileReference ref = getForPath(storeable.getStrategy(), storeable.getPath());
        return doStore(in, storeable.getPath(), containerPrefix + storeable.getStrategy().getRootPath(), ref);
    }

    @Override
    public LocalFileReference copy(LocalFileReference ref, Storeable targetStoreable) throws IOException {
        return ref.copyTo(targetStoreable.getPath());
    }

    @Override
    public LocalFileReference rename(LocalFileReference ref, Storeable targetStoreable) throws IOException {
        return ref.renameTo(targetStoreable.getPath());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // The map of hash resolvers must at least have an entry under the default key.
        if (!hashResolvers.containsKey(FileHashResolver.STORE_NAME_DEFAULT)) {
            throw new IllegalArgumentException("No default hash resolver provided");
        }
    }

    @Override
    public void destroy() throws Exception {
        executionService.shutdown();
    }

}
