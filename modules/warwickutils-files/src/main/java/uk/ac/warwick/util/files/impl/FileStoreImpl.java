package uk.ac.warwick.util.files.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.core.spring.FileUtils;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.FileReferenceCreationStrategy;
import uk.ac.warwick.util.files.FileReferenceCreationStrategy.Target;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.LocalFileReference;
import uk.ac.warwick.util.files.LocalFileStore;
import uk.ac.warwick.util.files.Storeable;
import uk.ac.warwick.util.files.Storeable.StorageStrategy.MissingContentStrategy;
import uk.ac.warwick.util.files.UploadedFileDetails;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.HashString;

/**
 * File store which can ask a FileReferenceCreationStrategy
 */
public final class FileStoreImpl implements LocalFileStore, HashFileStore, InitializingBean {
    
    private final TemporaryUploadsDirectoryHolder uploadDirHolder;
    
    private final Map<String,FileHashResolver> hashResolvers;
    
    private final FileReferenceCreationStrategy storageStrategy;
    
    public FileStoreImpl(TemporaryUploadsDirectoryHolder holder, Map<String,FileHashResolver> resolvers, FileReferenceCreationStrategy strategy) {
        this.uploadDirHolder = holder;
        this.hashResolvers = resolvers;
        this.storageStrategy = strategy;
    }

    public FileReference store(Storeable storeable, String requestedStoreName, UploadedFileDetails uploadedFile) throws IOException {
        FileReferenceCreationStrategy.Target target = storageStrategy.select(uploadedFile);
        
        switch (target) {
            case local:
                return storeLocalReference(storeable, uploadedFile);
            case hash:
                return storeHashReference(uploadedFile, requestedStoreName);
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
    
    public FileReference store(Storeable storeable, String requestedStoreName, UsingOutput delegate) throws IOException {
        
        // Store to a temporary location
        File tmpDir = uploadDirHolder.getTemporaryUploadedFileDirectory();
        File tmpFile = FileUtils.createFile("fileStore", tmpDir);
        
        try {
            // Write out to a temporary file. We have to so that we can read it twice - 
            // once to get the storage strategy, and again to actually store it.
            // (three times if hashing, as we read the whole file to get its hash)
            FileOutputStream os = new FileOutputStream(tmpFile);
            try {
                delegate.doWith(os);
            } finally {
                os.close();
            }
            
            Target strategy = storageStrategy.select(tmpFile);
            
            switch (strategy) {
                case local:
                    File newFile = resolve(storeable);
                    return doStore(storeable, tmpFile, newFile);
                case hash:
                    return storeHashReference(tmpFile, requestedStoreName);
                default:
                    throw new IllegalStateException("Unhandled strategy; " + strategy);
            }
        } finally {
            uk.ac.warwick.util.core.spring.FileUtils.recursiveDelete(tmpFile, false);
        }        
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

    public HashFileReference createHashReference(UsingOutput callback, String requestedStoreName) throws IOException {
        // Store to a temporary location
        File tmpDir = uploadDirHolder.getTemporaryUploadedFileDirectory();
        File tmpFile = FileUtils.createFile("hashReference", tmpDir);
        
        try {
            // Write out to a temporary file.
            FileOutputStream os = new FileOutputStream(tmpFile);
            try {
                callback.doWith(os);
            } finally {
                os.close();
            }
            
            return storeHashReference(tmpFile,requestedStoreName);
        } finally {
            uk.ac.warwick.util.core.spring.FileUtils.recursiveDelete(tmpFile, false);
        }
    }

    public HashFileReference storeHashReference(UploadedFileDetails uploadedFile, String requestedStoreName) throws IOException {
        FileHashResolver hashResolver = getHashResolver(requestedStoreName);
        
        HashString hash = hashResolver.generateHash(uploadedFile.getContents());
        
        HashFileReference existing = hashResolver.lookupByHash(this, hash, true);
        if (existing.isExists()) {
            return existing;
        }
        
        return doStore(uploadedFile.getContents(), hash, existing);
    }
    
    private HashFileReference doStore(InputStream is, HashString hash, HashFileReference target) throws IOException {
        if (!target.isFileBacked()) {
            throw new UnsupportedOperationException("Storage to non-file backed data not implemented");
        }
        
        File outputFile = target.getRealFile();
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
        return resolve(storeable, storeable.getPath());
    }
    
    private File resolve(Storeable storeable, String path) {
        File rootDirectory = storeable.getStrategy().getRootDirectory();
        Assert.notNull(rootDirectory);
        File newFile = new File(rootDirectory, path);
        return newFile;
    }

    public LocalFileReference getForPath(Storeable storeable, String path) {
        File newFile = resolve(storeable, path);
        
        // If the file doesn't exist, we still return a file backed reference,
        // and just allow the delegation to file.exists() to do its work.
        return new FileBackedFileReference(this, newFile, path, storeable.getStrategy());
    }
    
    public HashFileReference getByFileHash(HashString fileHash) {
        return getHashResolver(fileHash).lookupByHash(this, fileHash, false);
    }

    public FileReference get(Storeable storeable) throws FileNotFoundException {
        if (storeable.getHash() != null && !storeable.getHash().isEmpty()) {
            return getByFileHash(storeable.getHash());
        } else {
            FileReference ref;
            LocalFileReference localRef = null;
            if (storeable.getStrategy().isSupportsLocalReferences() || storeable.getStrategy().getMissingContentStrategy() == MissingContentStrategy.Local) {
                localRef = getForPath(storeable, storeable.getPath());
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

    public LocalFileReference storeLocalReference(Storeable storeable, UploadedFileDetails uploadedFile) throws IOException {
        File target = resolve(storeable);
        File source = uploadedFile.getFile();
        
        return doStore(storeable, source, target);
    }
    
    public LocalFileReference copy(LocalFileReference ref, Storeable targetStoreable) throws IOException {
        File target = resolve(targetStoreable);
        File source = ref.getFile();
        
        return doStore(targetStoreable, source, target);
    }
    
    public LocalFileReference rename(LocalFileReference ref, Storeable targetStoreable) throws IOException {
        File target = resolve(targetStoreable);
        File source = ref.getFile();
        
        if (!target.getParentFile().exists()) {
            Assert.isTrue(target.getParentFile().mkdirs(), "Couldn't create dirs for " + target);
        }
        
        if (!source.renameTo(target)) {
            throw new IOException("Could not rename " + source + " to " + target);
        }
        
        return new FileBackedFileReference(this, target, targetStoreable.getPath(), ref.getStorageStrategy());
    }
    
    private LocalFileReference doStore(Storeable storeable, File source, File target) throws IOException {
        if (target.exists()) {
            throw new IllegalArgumentException("Path " + target + " already exists");
        }
        
        if (!target.getParentFile().exists()) {
            Assert.isTrue(target.getParentFile().mkdirs(), "Couldn't create dirs for " + target);
        }
        
        FileCopyUtils.copy(source, target);
        return new FileBackedFileReference(this, target, storeable.getPath(), storeable.getStrategy());
    }

    public void afterPropertiesSet() throws Exception {
        // The map of hash resolvers must at least have an entry under the default key.
        if (!hashResolvers.containsKey(FileHashResolver.STORE_NAME_DEFAULT)) {
            throw new IllegalArgumentException("No default hash resolver provided");
        }
    }

}
