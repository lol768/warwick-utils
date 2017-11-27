package uk.ac.warwick.util.convert;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.google.common.io.ByteSource;
import uk.ac.warwick.util.core.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;

public class S3ByteSource extends ByteSource {

    private AmazonS3 s3;
    private String bucket;
    private String key;

    private final long offset;
    private final long length;
    private transient long s3ContentLength = -1;

    public S3ByteSource(AmazonS3 s3, String bucket, String key) {
        this(s3, bucket, key, 0);
    }

    public S3ByteSource(AmazonS3 s3, String bucket, String key, long offset) {
        this(s3, bucket, key, offset, Long.MAX_VALUE);
    }

    public S3ByteSource(AmazonS3 s3, String bucket, String key, long offset, long length) {
        this(s3, bucket, key, offset, length, -1);
    }

    public S3ByteSource(AmazonS3 s3, String bucket, String key, long offset, long length, long s3ContentLength) {
        this.s3 = s3;
        this.bucket = bucket;
        this.key = key;
        this.offset = offset;
        this.length = length;
        this.s3ContentLength = s3ContentLength;
        validate();
    }

    private void validate() {
        if (s3 == null) {
            throw new IllegalArgumentException("Uninitialised Amazon S3 instance");
        }
        if (!StringUtils.hasText(bucket)) {
            throw new IllegalArgumentException("Missing Amazon S3 bucket name");
        }
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("Missing Amazon S3 object name");
        }
    }

    private void checkExists() throws IOException {
        if (!s3.doesObjectExist(bucket, key)) {
            throw new IOException("Amazon S3 object not found");
        }
    }

    private long getS3ContentLength() {
        if (s3ContentLength < 0) {
            s3ContentLength = s3.getObjectMetadata(bucket, key).getContentLength();
        }
        return this.s3ContentLength;
    }

    private long getEnd() {
        return offset + length - 1;
    }

    private Date getS3LastModified() {
        return s3.getObjectMetadata(bucket, key).getLastModified();
    }

    public synchronized Instant getLastModified() throws IOException {
        checkExists();

        return getS3LastModified().toInstant();
    }

    @Override
    public synchronized InputStream openStream() throws IOException {
        checkExists();

        // The stream from an s3 object is time-limited and not repeatable; get a new object every time
        GetObjectRequest req = new GetObjectRequest(bucket, key).withRange(offset, getEnd());
        return s3.getObject(req).getObjectContent();
    }

    @Override
    public synchronized boolean isEmpty() {
        try {
            checkExists();
            return getS3ContentLength() > 0;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public synchronized long size() {
        try {
            checkExists();
            return Math.min(length, getS3ContentLength() - offset);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public ByteSource slice(long offset, long length) {
        long newOffset = this.offset + offset;
        long newLength = Math.min(length, getEnd() - newOffset);

        return new S3ByteSource(s3, bucket, key, newOffset, newLength, getS3ContentLength());
    }

    @Override
    public String toString() {
        return "S3ByteSource.asByteSource(" + bucket + "/" + key + ")";
    }
}
