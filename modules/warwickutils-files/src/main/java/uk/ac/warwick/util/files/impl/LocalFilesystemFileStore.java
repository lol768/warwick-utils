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
public final class LocalFilesystemFileStore extends AbstractFileStore implements InitializingBean {
    
    public LocalFilesystemFileStore(Map<String,FileHashResolver> resolvers, FileReferenceCreationStrategy strategy) {
        super(resolvers, strategy);
    }

    @Override
    protected HashFileReference doStore(ByteSource in, HashString hash, HashFileReference target) throws IOException {
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

        try (FileOutputStream os = new FileOutputStream(outputFile)) {
            in.copyTo(os);
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

}
