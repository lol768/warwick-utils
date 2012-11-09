package uk.ac.warwick.util.files.hash;

import uk.ac.warwick.util.core.StringUtils;

/**
 * Unique identifier for a piece of data that has been placed into
 * a hash-based store. At its simplest, this will just contain a
 * hash value. It can also be qualified with the name of a hash
 * store where the data is located. 
 * 
 * If the input string is unqualified then it is equivalent to 
 * specifying the "default" hash store - in fact it's the other way
 * round. If you specify a storeName of "default" or null then the
 * resultant HashString is automatically unqualified.
 */
public final class HashString {
    
    public static final char SEPARATOR_CHAR = '/';
    
    public static final String DEFAULT_STORE = "default";
    
    private String qualifiedHash;
    private String hash;
    private String store;
    
    /**
     * Create a HashString from a combined qualified or unqualified
     * string, as you might fetch from the database. If a qualifier
     * is present but equal to {@value #DEFAULT_STORE} then it is
     * treated as unqualified.
     */
    public HashString(String qualifiedHashString) {
        parse(qualifiedHashString);
    }
    
    /**
     * Equivalent to {@link #HashString(String)} but you can specify
     * the store name and hash value separately.
     * 
     * @param storeName name of store or null if unqualified
     * @param hashed
     */
    public HashString(String storeName, String hashed) {
        setData(storeName, hashed);
    }
    
    private void setData(String storeName, String hashed) {
        if (storeName == null || DEFAULT_STORE.equals(storeName)) {
            this.qualifiedHash = hashed;
            this.hash = hashed;
        } else {
            this.qualifiedHash = storeName + SEPARATOR_CHAR + hashed;
            this.hash = hashed;
            this.store = storeName;
        }
    }

    /**
     * Get the name of the store. Use {@link #isDefaultStore()} first
     * to check whether it specifies a particular store.
     * 
     * @return The name of the hash store if specified, otherwise null.
     */
    public String getStoreName() {
        return store;
    }
    
    /**
     * @return The unqualified hash string.
     */
    public String getHash() {
        return hash;
    }
    
    public boolean isEmpty() {
        return !StringUtils.hasLength(qualifiedHash);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof HashString)) {
            return false;
        }
        
        return qualifiedHash.equals(((HashString)obj).qualifiedHash);
    }
    
    @Override
    public int hashCode() {
        return qualifiedHash.hashCode();
    }
    
    /**
     * Returns the full qualified hash string.
     * <p>
     * <strong>DO NOT EVER CHANGE THIS</strong>
     */
    public String toString() {
        return qualifiedHash;
    }
    
    /**
     * If the string was not qualified with a store name, we
     * use the default store.
     */
    public boolean isDefaultStore() {
        return store == null || "default".equals(store);
    }
    
    private void parse(String qualified) {
        qualifiedHash = qualified;
        if (isEmpty()) {
            return;
        }
        int index = qualified.indexOf(SEPARATOR_CHAR);
        if (index < 0) {
            setData(null, qualified);
        } else {
            setData(qualified.substring(0,index), qualified.substring(index+1));
        }
        
        if (hash.isEmpty()) {
            throw new IllegalArgumentException("Hash part must not be empty");
        }
    }
}
