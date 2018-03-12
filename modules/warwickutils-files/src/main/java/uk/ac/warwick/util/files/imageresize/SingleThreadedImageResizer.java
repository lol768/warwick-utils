package uk.ac.warwick.util.files.imageresize;

import com.google.common.io.ByteSource;
import uk.ac.warwick.util.concurrency.TaskExecutionService;
import uk.ac.warwick.util.files.hash.HashString;

import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

public final class SingleThreadedImageResizer implements ImageResizer {

    private final TaskExecutionService executionService = new TaskExecutionService(1); // Single execution thread

    private final ConcurrentMap<String, Future<Long>> lengthRequests = new ConcurrentHashMap<String, Future<Long>>();

    private final ImageResizer delegate;

    public SingleThreadedImageResizer(ImageResizer resizer) {
        this.delegate = resizer;
    }

    public long getResizedImageLength(final ByteSource source, final HashString hash, final ZonedDateTime lastModified, final int maxWidth, final int maxHeight, final FileType fileType) throws IOException {
        String key = generateCacheKey(hash, maxWidth, maxHeight);

        try {
            Future<Long> future;

            if (lengthRequests.containsKey(key)) {
                future = lengthRequests.get(key);
            } else {
                future = executionService.submit(new Callable<Long>() {
                    public Long call() throws Exception {
                        return delegate.getResizedImageLength(source, hash, lastModified, maxWidth, maxHeight, fileType);
                    }
                });

                lengthRequests.put(key, future);
            }

            return future.get();
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            lengthRequests.remove(key);
        }
    }

    public void renderResized(final ByteSource source, final HashString hash, final ZonedDateTime lastModified, final OutputStream out, final int maxWidth, final int maxHeight, final FileType fileType)
            throws IOException {
        // OutputStream will change between requests so we can't cache this
        try {
            executionService.submit(new Callable<Void>() {
                public Void call() throws Exception {
                    delegate.renderResized(source, hash, lastModified, out, maxWidth, maxHeight, fileType);

                    return null;
                }
            }).get();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private String generateCacheKey(final HashString hash, final int maxWidth, final int maxHeight) {
        String referencePath = hash.toString();

        return referencePath + "@" + maxWidth + "x" + maxHeight;
    }

}
