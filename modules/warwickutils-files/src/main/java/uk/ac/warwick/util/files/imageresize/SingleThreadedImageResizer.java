package uk.ac.warwick.util.files.imageresize;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import org.joda.time.DateTime;

import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.concurrency.TaskExecutionService;

public final class SingleThreadedImageResizer implements ImageResizer {
    
    private final TaskExecutionService executionService = new TaskExecutionService(1); // Single execution thread
    
    private final ConcurrentMap<String, Future<Long>> lengthRequests = new ConcurrentHashMap<String, Future<Long>>();
    
    private final ImageResizer delegate;
    
    public SingleThreadedImageResizer(ImageResizer resizer) {
        this.delegate = resizer;
    }

    public long getResizedImageLength(final FileReference sourceFile, final DateTime lastModified, final int maxWidth, final int maxHeight, final FileType fileType) throws IOException {
        String key = generateCacheKey(sourceFile, maxWidth, maxHeight);
        
        try {
            Future<Long> future;
            
            if (lengthRequests.containsKey(key)) {
                future = lengthRequests.get(key);
            } else {
                future = executionService.submit(new Callable<Long>() {
                    public Long call() throws Exception {
                        return delegate.getResizedImageLength(sourceFile, lastModified, maxWidth, maxHeight, fileType);
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

    public void renderResized(final FileReference sourceFile, final DateTime lastModified, final OutputStream out, final int maxWidth, final int maxHeight, final FileType fileType)
            throws IOException {
        // OutputStream will change between requests so we can't cache this
        try {
            executionService.submit(new Callable<Void>() {
                public Void call() throws Exception {
                    delegate.renderResized(sourceFile, lastModified, out, maxWidth, maxHeight, fileType);
                    
                    return null;
                }
            }).get();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    private String generateCacheKey(final FileReference sourceFile, final int maxWidth, final int maxHeight) {
        String referencePath = sourceFile.toHashReference().getHash().toString();
        
        return referencePath + "@" + maxWidth + "x" + maxHeight;
    }

}
