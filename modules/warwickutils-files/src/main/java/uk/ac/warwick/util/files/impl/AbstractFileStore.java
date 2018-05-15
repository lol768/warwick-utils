package uk.ac.warwick.util.files.impl;

import com.google.common.io.ByteSource;
import org.springframework.beans.factory.InitializingBean;
import uk.ac.warwick.util.files.DefaultFileStoreStatistics;
import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.files.FileReferenceCreationStrategy;
import uk.ac.warwick.util.files.FileStoreStatistics;
import uk.ac.warwick.util.files.HashFileReference;
import uk.ac.warwick.util.files.HashFileStore;
import uk.ac.warwick.util.files.LocalFileReference;
import uk.ac.warwick.util.files.LocalFileStore;
import uk.ac.warwick.util.files.Storeable;
import uk.ac.warwick.util.files.hash.FileHashResolver;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public abstract class AbstractFileStore implements LocalFileStore, HashFileStore, InitializingBean {

    private final Map<String, FileHashResolver> hashResolvers;

    private final FileReferenceCreationStrategy storageStrategy;

    protected FileStoreStatistics statistics = new DefaultFileStoreStatistics(this);

    public AbstractFileStore(Map<String,FileHashResolver> resolvers, FileReferenceCreationStrategy strategy) {
        this.hashResolvers = resolvers;
        this.storageStrategy = strategy;
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

    @Override
    public HashFileReference createHashReference(ByteSource in, String requestedStoreName) throws IOException {
        return storeHashReference(in, requestedStoreName);
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

    abstract HashFileReference doStore(ByteSource in, HashString hash, HashFileReference target) throws IOException;

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
            if (storeable.getStrategy().isSupportsLocalReferences() || storeable.getStrategy().getMissingContentStrategy() == Storeable.StorageStrategy.MissingContentStrategy.Local) {
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
    public void afterPropertiesSet() throws Exception {
        // The map of hash resolvers must at least have an entry under the default key.
        if (!hashResolvers.containsKey(FileHashResolver.STORE_NAME_DEFAULT)) {
            throw new IllegalArgumentException("No default hash resolver provided");
        }
    }

    public FileStoreStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(FileStoreStatistics statistics) {
        this.statistics = statistics;
    }
}
