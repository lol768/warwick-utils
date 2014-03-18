package uk.ac.warwick.util.cache;

public final class CacheStoreUnavailableException extends Exception {

    public CacheStoreUnavailableException(String message) {
        super(message);
    }

    public CacheStoreUnavailableException(Throwable cause) {
        super(cause);
    }

}
