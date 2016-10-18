package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import uk.ac.warwick.util.files.*;
import uk.ac.warwick.util.files.Storeable.StorageStrategy.MissingContentStrategy;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.*;
import java.util.Map;
import java.util.stream.Stream;

/**
 * File store which can ask a FileReferenceCreationStrategy
 */
public final class LocalFilesystemFileStore implements LocalFileStore, HashFileStore, InitializingBean {

    private final Map<String, FileHashResolver> hashResolvers;

    private final FileReferenceCreationStrategy storageStrategy;
    
    public LocalFilesystemFileStore(Map<String,FileHashResolver> resolvers, FileReferenceCreationStrategy strategy) {
        this.hashResolvers = resolvers;
        this.storageStrategy = strategy;
    }

    @Override
    public FileReference store(Storeable storeable, String requestedStoreName, ByteSource in) throws IOException {
        FileReferenceCreationStrategy.Target target = storageStrategy.select(in);

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

    private HashFileReference storeHashReference(File tmpFile, String requestedHashStore) throws IOException {
        // If there is an existing hash reference, return that; otherwise, store the physical file
        FileHashResolver hashResolver = getHashResolver(requestedHashStore);
        FileInputStream is = new FileInputStream(tmpFile);

        HashString hash;
        try {
            hash = hashResolver.generateHash(is);
        } finally {
            is.close();
        }

        HashFileReference existing = hashResolver.lookupByHash(this, hash, true);
        if (existing.isExists()) {
            return existing;
        }

        is = new FileInputStream(tmpFile);
        try {
            return doStore(is, hash, existing);
        } finally {
            is.close();
        }
    }

    @Override
    public HashFileReference createHashReference(ByteSource in, String requestedStoreName) throws IOException {
        return storeHashReference(in, requestedStoreName);
    }

    @Override
    public HashFileReference storeHashReference(ByteSource in, String requestedStoreName) throws IOException {
        FileHashResolver hashResolver = getHashResolver(requestedStoreName);
        
        HashString hash = hashResolver.generateHash(in.openBufferedStream());
        
        HashFileReference existing = hashResolver.lookupByHash(this, hash, true);
        if (existing.isExists()) {
            return existing;
        }
        
        return doStore(in.openStream(), hash, existing);
    }
    
    private HashFileReference doStore(InputStream is, HashString hash, HashFileReference target) throws IOException {
        if (!target.isFileBacked()) {
            throw new UnsupportedOperationException("Storage to non-file backed data not implemented");
        }
        
        File outputFile = new File(target.getFileLocation().getPath());
        if (outputFile.exists()) {
            throw new IllegalArgumentException("Output file already exists for " + outputFile);
        }
        
        if ((!outputFile.getParentFile().exists() || !outputFile.getParentFile().isDirectory()) && !outputFile.getParentFile().mkdirs()) {
            throw new IllegalStateException("Could not create parent directory for " + outputFile);
        }
        
        FileOutputStream os = new FileOutputStream(outputFile);
        try {
            FileCopyUtils.copy(is, os);
        } finally {
            os.close();
        }
        
        return target;
    }

    private File resolve(Storeable storeable) {
        return resolve(storeable.getStrategy(), storeable.getPath());
    }
    
    private File resolve(Storeable.StorageStrategy storageStrategy, String path) {
        File rootDirectory = new File(storageStrategy.getRootPath());
        Assert.notNull(rootDirectory);
        return new File(rootDirectory, path);
    }

    @Override
    public LocalFileReference getForPath(Storeable.StorageStrategy storageStrategy, String path) {
        File newFile = resolve(storageStrategy, path);
        
        // If the file doesn't exist, we still return a file backed reference,
        // and just allow the delegation to file.exists() to do its work.
        return new FileBackedLocalFileReference(this, newFile, path, storageStrategy);
    }

    @Override
    public Stream<String> list(Storeable.StorageStrategy storageStrategy, String basePath) {
        File dir = resolve(storageStrategy, basePath);
        String[] fileNames = dir.list();
        if (fileNames == null) {
            // Not a directory
            return Stream.empty();
        } else {
            return Stream.of(fileNames);
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
        File target = resolve(storeable);
        
        return doStore(storeable, in.openBufferedStream(), target);
    }
    
    public LocalFileReference copy(LocalFileReference ref, Storeable targetStoreable) throws IOException {
        File target = resolve(targetStoreable);
        File source = new File(ref.getFileLocation().getPath());
        
        return doStore(targetStoreable, new FileInputStream(source), target);
    }
    
    public LocalFileReference rename(LocalFileReference ref, Storeable targetStoreable) throws IOException {
        File target = resolve(targetStoreable);
        File source = new File(ref.getFileLocation().getPath());
        
        if (!target.getParentFile().exists()) {
            Assert.isTrue(target.getParentFile().mkdirs(), "Couldn't create dirs for " + target);
        }
        
        if (!source.renameTo(target)) {
            throw new IOException("Could not rename " + source + " to " + target);
        }
        
        return new FileBackedLocalFileReference(this, target, targetStoreable.getPath(), ref.getStorageStrategy());
    }
    
    private LocalFileReference doStore(Storeable storeable, InputStream in, File target) throws IOException {
        if (target.exists()) {
            throw new IllegalArgumentException("Path " + target + " already exists");
        }
        
        if (!target.getParentFile().exists()) {
            Assert.isTrue(target.getParentFile().mkdirs(), "Couldn't create dirs for " + target);
        }
        
        FileCopyUtils.copy(in, new FileOutputStream(target));
        return new FileBackedLocalFileReference(this, target, storeable.getPath(), storeable.getStrategy());
    }

    public void afterPropertiesSet() throws Exception {
        // The map of hash resolvers must at least have an entry under the default key.
        if (!hashResolvers.containsKey(FileHashResolver.STORE_NAME_DEFAULT)) {
            throw new IllegalArgumentException("No default hash resolver provided");
        }
    }

}
