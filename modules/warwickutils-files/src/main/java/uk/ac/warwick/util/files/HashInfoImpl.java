package uk.ac.warwick.util.files;

import uk.ac.warwick.util.files.hash.HashString;

import java.time.LocalDateTime;

public final class HashInfoImpl implements HashInfo {

    private String hash;
    private LocalDateTime createdDate;
    private Long fileSize;
    
    @SuppressWarnings("unused")
    private HashInfoImpl() {
        //for Hibernate's delectation
    }
    
    public HashInfoImpl(final HashString h, final LocalDateTime theCreatedDate, Long theFileSize ) {
        this.hash = h.toString();
        this.createdDate = theCreatedDate;
        this.fileSize = theFileSize;
    }

    public String getHash() {
        return this.hash;
    }

    public LocalDateTime getCreatedDate() {
        return this.createdDate;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

}
