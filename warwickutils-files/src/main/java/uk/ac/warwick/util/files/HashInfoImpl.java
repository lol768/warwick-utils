package uk.ac.warwick.util.files;

import org.joda.time.DateTime;

import uk.ac.warwick.util.files.hash.HashString;


public final class HashInfoImpl implements HashInfo {

    private String hash;
    private DateTime createdDate;
    private Long fileSize;
    
    @SuppressWarnings("unused")
    private HashInfoImpl() {
        //for Hibernate's delectation
    }
    
    public HashInfoImpl(final HashString h, final DateTime theCreatedDate, Long theFileSize ) {
        this.hash = h.toString();
        this.createdDate = theCreatedDate;
        this.fileSize = theFileSize;
    }

    public String getHash() {
        return this.hash;
    }

    public DateTime getCreatedDate() {
        return this.createdDate;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

}
