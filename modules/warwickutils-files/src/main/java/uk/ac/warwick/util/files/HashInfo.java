package uk.ac.warwick.util.files;

import java.time.LocalDateTime;

/**
 * This is literally just to represent the file_hash table in
 * Hibernate. We don't really need to use this outside of the DAO.
 */
public interface HashInfo {

    String getHash();
    
    LocalDateTime getCreatedDate();
    
    Long getFileSize();

}