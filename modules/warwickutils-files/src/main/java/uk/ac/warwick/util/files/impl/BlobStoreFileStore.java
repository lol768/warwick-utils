package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.MultipartPart;
import org.jclouds.blobstore.domain.MultipartUpload;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
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
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.FileReferenceCreationStrategy;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.LocalFileReference;
import uk.ac.warwick.util.files.Storeable;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.IOException;
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
public final class BlobStoreFileStore extends AbstractFileStore implements InitializingBean, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlobStoreFileStore.class);

    private final BlobStore blobStore;

    private final String containerPrefix;

    private final PayloadSlicer payloadSlicer = new BasePayloadSlicer();

    private final TaskExecutionService executionService = new TaskExecutionService(Executors.newCachedThreadPool());

    public BlobStoreFileStore(Map<String, FileHashResolver> resolvers, FileReferenceCreationStrategy strategy, BlobStoreContext context, String containerPrefix) {
        super(resolvers, strategy);
        this.blobStore = context.getBlobStore();
        this.containerPrefix = containerPrefix;
    }

    @Override
    protected HashFileReference doStore(ByteSource in, HashString hash, HashFileReference target) throws IOException {
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

        statistics.time(() -> {
            try {
                doPut(container, blob, size);
            } catch (HttpResponseException e) {
                // PHO-247
                LOGGER.warn("PUT: HttpResponseException encountered; might be a 401, so checking for fresh tokens and retrying...");

                // almost any other request will handle a 401 by refreshing the auth token.
                blobStore.blobMetadata(container, key);
                doPut(container, blob, size);
            }
        }, statistics::referenceWritten);

        return target;
    }

    @SuppressWarnings("UnstableApiUsage")
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
        return statistics.timeSafe(() -> {
            String containerName = containerPrefix + storageStrategy.getRootPath();

            PageSet<? extends StorageMetadata> firstResults = blobStore.list(containerName, ListContainerOptions.Builder.prefix(basePath).recursive());

            return listKeys(containerName, basePath, firstResults.getNextMarker(), firstResults.stream().map(StorageMetadata::getName));
        }, statistics::traversed);
    }

    private Stream<String> listKeys(String containerName, String prefix, String nextMarker, Stream<String> accumulator) {
        if (nextMarker == null) {
            // No more results
            return accumulator;
        } else {
            PageSet<? extends StorageMetadata> nextResults = blobStore.list(containerName, ListContainerOptions.Builder.prefix(prefix).afterMarker(nextMarker).recursive());

            return Stream.concat(accumulator, listKeys(containerName, prefix, nextResults.getNextMarker(), nextResults.stream().map(StorageMetadata::getName)));
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
    public void destroy() {
        executionService.shutdown();
    }

}
