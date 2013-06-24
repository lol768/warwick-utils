package uk.ac.warwick.util.files.imageresize;

import java.awt.RenderingHints;
import java.awt.image.renderable.ParameterBlock;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.util.FileCopyUtils;

import uk.ac.warwick.util.files.FileReference;
import uk.ac.warwick.util.collections.Pair;

import com.google.common.collect.Maps;
import com.sun.media.jai.codec.ByteArraySeekableStream;
import com.sun.media.jai.codec.FileCacheSeekableStream;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.media.jai.codec.SeekableStream;

public final class JAIImageResizer implements ImageResizer {
    public static final float DEFAULT_SAMPLING_QUALITY = 0.8f;
    public static final int DEFAULT_MAX_WIDTH = 8000;
    public static final int DEFAULT_MAX_HEIGHT = 5000;

    private static final Logger LOGGER = Logger.getLogger(JAIImageResizer.class);
    
    private Interpolation interpolation = Interpolation.getInstance(Interpolation.INTERP_BICUBIC);
    
    private boolean useSubsampleAveraging = true;
    private int maxWidthToResize = DEFAULT_MAX_WIDTH;
    private int maxHeightToResize = DEFAULT_MAX_HEIGHT;

    void renderResized(final byte[] sourceBytes, final OutputStream out, final int maxWidth, final int maxHeight,
            final FileType fileType) throws IOException {
        Pair<Integer, Integer> dimensions = getDimensions(new ByteArrayInputStream(sourceBytes));
        if (isOversized(dimensions.getLeft(), dimensions.getRight())) {
            FileCopyUtils.copy(sourceBytes, out);
            return;
        }
        
        long start = System.currentTimeMillis();
        SeekableStream sourceBASS = new ByteArraySeekableStream(sourceBytes);
        renderResizedStream(out, maxWidth, maxHeight, start, sourceBASS, fileType, dimensions.getLeft(), dimensions.getRight());
    }

    public void renderResized(final File sourceFile, final OutputStream out, final int maxWidth, final int maxHeight,
            final FileType fileType) throws IOException {
        Pair<Integer, Integer> dimensions = getDimensions(new FileInputStream(sourceFile));
        if (isOversized(dimensions.getLeft(), dimensions.getRight())) {
            FileCopyUtils.copy(new FileInputStream(sourceFile), out);
            return;
        }
        
        long start = System.currentTimeMillis();
        SeekableStream sourceBASS = new FileSeekableStream(sourceFile);
        renderResizedStream(out, maxWidth, maxHeight, start, sourceBASS, fileType, dimensions.getLeft(), dimensions.getRight());
    }
    
    public void renderResized(final FileReference sourceFile, final DateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight,
            final FileType fileType) throws IOException {
        Pair<Integer, Integer> dimensions = getDimensions(sourceFile.getInputStream());
        if (isOversized(dimensions.getLeft(), dimensions.getRight())) {
            LOGGER.warn("Refusing to resize image of dimensions " + dimensions.getLeft() + "x" + dimensions.getRight() + ": " + sourceFile);
            FileCopyUtils.copy(sourceFile.getInputStream(), out);
            return;
        }
        
        long start = System.currentTimeMillis();
        SeekableStream sourceBASS = new FileCacheSeekableStream(sourceFile.getInputStream());
        renderResizedStream(out, maxWidth, maxHeight, start, sourceBASS, fileType, dimensions.getLeft(), dimensions.getRight());
    }

    private void renderResizedStream(final OutputStream out, final int maxWidth, final int maxHeight, final long start,
            final SeekableStream sourceSS, final FileType fileType, float width, float height) throws IOException {
        try {
            PlanarImage source = JAI.create("stream", sourceSS).createInstance();
    
            // assume no resizing at first
            double scale = 1;
            
            if (!shouldResizeWidth(width, maxWidth) && !shouldResizeHeight(height, maxHeight)) {
                // stream the input into the output
                // seek back to the start of the stream first
                sourceSS.seek(0);
                FileCopyUtils.copy(sourceSS, out);
                return;
            }
    
            // if the image is too wide, scale down
            if (shouldResizeWidth(width, maxWidth)) {
                scale = maxWidth / width;
            }
    
            // if the image is too hight, scale down
            // IF that makes it smaller than maxWidth has done already
            if (shouldResizeHeight(height, maxHeight)) {
                float heightScale = maxHeight / height;
                if (heightScale < scale) {
                    scale = heightScale;
                }
            }
    
            ParameterBlock params = new ParameterBlock();
            params.addSource(source);
            params.add(scale);// x scale factor
            params.add(scale);// y scale factor
            params.add(0.0F);// x translate
            params.add(0.0F);// y translate
            
            Map<RenderingHints.Key, Object> map = Maps.newHashMap();
            map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            map.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            RenderingHints hints = new RenderingHints(map);
    
            params.add(interpolation);
            
            String operation = useSubsampleAveraging ? "SubsampleAverage" : "scale";
            PlanarImage planarImage = JAI.create(operation, params, hints).getRendering();
    
            ImageEncoder encoder;
    
            switch (fileType) {
                case gif:
                case jpg:
                    // now re-encode
                    JPEGEncodeParam jpegEncodeParam = new JPEGEncodeParam();
                    jpegEncodeParam.setQuality(DEFAULT_SAMPLING_QUALITY);
                    // who knows what all this could possibly mean ?
                    jpegEncodeParam.setHorizontalSubsampling(0, 1);
                    jpegEncodeParam.setHorizontalSubsampling(1, 2);
                    jpegEncodeParam.setHorizontalSubsampling(2, 2);
                    jpegEncodeParam.setVerticalSubsampling(0, 1);
                    jpegEncodeParam.setVerticalSubsampling(1, 1);
                    jpegEncodeParam.setVerticalSubsampling(2, 1);
                    final int restartInterval = 64;
                    jpegEncodeParam.setRestartInterval(restartInterval);
    
                    // done messing with the image. Send the bytes to the
                    // outputstream.
                    encoder = ImageCodec.createImageEncoder("JPEG", out, jpegEncodeParam);
    
                    break;
                case png:
                    PNGEncodeParam.RGB pngEncodeParam = new PNGEncodeParam.RGB();
                    encoder = ImageCodec.createImageEncoder("PNG", out, pngEncodeParam);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognised image");
            }
    
            encoder.encode(planarImage);
    
            long duration = System.currentTimeMillis() - start;
            LOGGER.debug("MS to Resize image: " + duration);
        } catch (Exception e) {
            LOGGER.error("Exception when resizing image, returning original image", e);
            sourceSS.seek(0);
            FileCopyUtils.copy(sourceSS, out);
        }
    }
    
    public long getResizedImageLength(FileReference sourceFile, DateTime lastModified, int maxWidth, int maxHeight, FileType fileType) throws IOException {
        // create a byte array output stream and then get the length of the byte array. clever, aye?
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        renderResized(sourceFile, lastModified, baos, maxWidth, maxHeight, fileType);
        baos.flush();
        baos.close();
        return baos.toByteArray().length;
    }

    private boolean shouldResizeWidth(final float width, final float maxWidth) {
        if (width > maxWidth && maxWidth != 0) {
            return true;
        }
        return false;
    }

    private boolean shouldResizeHeight(final float height, final float maxHeight) {
        if (height > maxHeight && maxHeight != 0) {
            return true;
        }
        return false;
    }
    
    public static Pair<Integer, Integer> getDimensions(InputStream input) throws IOException {
        // Optimisation: We don't actually need to read the whole image to get the width and height
        ImageInputStream imageStream = null;
        ImageReader reader = null;
        
        try {
            imageStream = ImageIO.createImageInputStream(input);
            
            // Safe to just call .next() as the NoSuchElementException will return null as desired
            reader = ImageIO.getImageReaders(imageStream).next();
            reader.setInput(imageStream, true, true);
            
            return Pair.of(reader.getWidth(0), reader.getHeight(0));
        } catch (NoSuchElementException e) { 
            throw new NotAnImageException(e);
        } finally {
            if (reader != null)
                reader.dispose();
            
            if (imageStream != null)
                imageStream.close();
            
            input.close();
        }
    }

    public boolean isOversized(int width, int height) {
        // total number of pixels is more important than exact measurements
        if (width * height > maxWidthToResize * maxHeightToResize) {
            return true;
        }
        
        return false;
    }

    public void setInterpolation(Interpolation interpolation) {
        this.interpolation = interpolation;
    }

    public void setUseSubsampleAveraging(boolean useSubsampleAveraging) {
        this.useSubsampleAveraging = useSubsampleAveraging;
    }

    public void setMaxWidthToResize(int maxWidth) {
        this.maxWidthToResize = maxWidth;
    }

    public void setMaxHeightToResize(int maxHeight) {
        this.maxHeightToResize = maxHeight;
    }
    
    static class NotAnImageException extends IOException {

        private static final long serialVersionUID = -3845937464714363305L;

        NotAnImageException(Throwable t) {
            super(t);
        }
    }

}
