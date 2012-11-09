package uk.ac.warwick.util.files.dao;

import java.util.List;

import org.joda.time.DateTime;

import uk.ac.warwick.util.files.HashInfo;
import uk.ac.warwick.util.files.hash.HashString;
import uk.ac.warwick.util.hibernate.BatchResults;

/**
 * This DAO is for manipulating and querying the
 * database table of hash strings. This should probably
 * only be used from the hash resolver.
 */
public interface HashInfoDAO {
    
    /**
     * Compares the table of all existent hashes with
     * all the tables where hashes are used, and returns
     * the ones that aren't used.
     * 
     * It's very important for this method to check ALL
     * the places where hashes are referenced, otherwise
     * this will return hashes that are actually in use,
     * which could cause them to be deleted.
     */
    Iterable<String> findUnreferencedHashes(DateTime createdBefore);

    /**
     * Check all the places where hashes are referenced and return whether the
     * hash is still in use. This is to reduce the Window of Scary-mary-ness
     * described in SBTWO-3854
     */
    boolean isUnreferenced(HashString hashString);
    
    /**
     * Get hashes created since a certain date (inclusive), ordered by Created Date
     */
    List<HashInfo> getHashesCreatedSince(DateTime createdSince, int returnCount);
    
    /**
     * Get hashes created before a certain date (exclusive), ordered by Created Date
     */
    List<HashInfo> getHashesCreatedBefore(DateTime createdBefore);
    
    BatchResults<HashInfo> scrollHashesCreatedBefore(DateTime createdBefore);
    
    /**
     * Get hashes created on a certain date, ordered by hash, starting from a particular hash
     */
    List<HashInfo> getHashesCreatedOn(DateTime createdOn, int returnCount, String startingHash);
    
    /**
     * When a hash is currently unused and is being
     * created, it must be added with this method so
     * that it can be tracked for references.
     */
    HashInfo hashCreated(HashString hash, long fileSize);
    
    HashInfo hashRemoved(HashString hash);
    
    HashInfo getHashById(String hash);
    HashInfo getHashByIdWithoutFlush(String hash);
    
    void clear();
}
