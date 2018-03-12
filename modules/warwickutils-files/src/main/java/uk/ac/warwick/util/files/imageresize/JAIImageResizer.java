package uk.ac.warwick.util.files.imageresize;

import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.sun.imageio.plugins.png.PNGMetadata;
import com.sun.media.jai.codec.SeekableStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.util.collections.Pair;
import uk.ac.warwick.util.files.hash.HashString;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

public final class JAIImageResizer implements ImageResizer {
    public static final float DEFAULT_SAMPLING_QUALITY = 0.8f;
    public static final int DEFAULT_MAX_WIDTH = 8000;
    public static final int DEFAULT_MAX_HEIGHT = 5000;

    private static final Logger LOGGER = LoggerFactory.getLogger(JAIImageResizer.class);

    private Interpolation interpolation = Interpolation.getInstance(Interpolation.INTERP_BICUBIC);

    private boolean useSubsampleAveraging = true;
    private int maxWidthToResize = DEFAULT_MAX_WIDTH;
    private int maxHeightToResize = DEFAULT_MAX_HEIGHT;

    @Override
    public void renderResized(final ByteSource source, final HashString hash, final ZonedDateTime entityLastModified, final OutputStream out, final int maxWidth, final int maxHeight,
                              final FileType fileType) throws IOException {
        Pair<Integer, Integer> dimensions = getDimensions(source.openStream());
        if (isOversized(dimensions.getLeft(), dimensions.getRight())) {
            LOGGER.warn("Refusing to resize image of dimensions " + dimensions.getLeft() + "x" + dimensions.getRight() + ": " + source);
            source.copyTo(out);
            return;
        }

        renderResizedStream(source, out, maxWidth, maxHeight, fileType, dimensions.getLeft(), dimensions.getRight());
    }

    private void renderResizedStream(final ByteSource in, final OutputStream out, final int maxWidth, final int maxHeight, final FileType fileType, float width, float height) throws IOException {
        if (!shouldResizeWidth(width, maxWidth) && !shouldResizeHeight(height, maxHeight)) {
            // stream the input into the output
            in.copyTo(out);
            return;
        }

        long start = System.currentTimeMillis();

        try (SeekableStream sourceSS = SeekableStream.wrapInputStream(in.openStream(), true)) {
            final RenderedImage renderedImage = ImageIO.read(sourceSS);
            final PlanarImage source = PlanarImage.wrapRenderedImage(renderedImage);

            // assume no resizing at first
            double scale = 1;

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

            ImageWriter writer;
            IIOMetadata metadata;
            ImageWriteParam writeParam;

            switch (fileType) {
                case gif:
                case jpg:
                    /**
                     * We used to do this with JAI but I'm not sure what the equivalent is.
                     * jpegEncodeParam.setHorizontalSubsampling(0, 1);
                     * jpegEncodeParam.setHorizontalSubsampling(1, 2);
                     * jpegEncodeParam.setHorizontalSubsampling(2, 2);
                     * jpegEncodeParam.setVerticalSubsampling(0, 1);
                     * jpegEncodeParam.setVerticalSubsampling(1, 1);
                     * jpegEncodeParam.setVerticalSubsampling(2, 1);
                     */

                    JPEGImageWriteParam param = new JPEGImageWriteParam(Locale.getDefault());
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(DEFAULT_SAMPLING_QUALITY);
                    writeParam = param;

                    metadata = null;

                    writer = ImageIO.getImageWritersByFormatName("JPEG").next();
                    writer.setOutput(new MemoryCacheImageOutputStream(out));

                    break;
                case png:
                    PNGMetadata pngMetadata = new PNGMetadata();
                    ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromRenderedImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
                    pngMetadata.initialize(imageTypeSpecifier, 3);
                    metadata = pngMetadata;
                    writer = ImageIO.getImageWritersByFormatName("PNG").next();
                    writer.setOutput(new MemoryCacheImageOutputStream(out));
                    writeParam = null;
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognised image");
            }

            IIOImage image = new IIOImage(planarImage, /*thumbnails=*/null, metadata);
            writer.write(metadata, image, writeParam);

            long duration = System.currentTimeMillis() - start;
            LOGGER.debug("MS to Resize image: " + duration);
        } catch (Exception e) {
            LOGGER.error("Exception when resizing image, returning original image", e);
            in.copyTo(out);
        }
    }

    @Override
    public long getResizedImageLength(ByteSource source, final HashString hash, final ZonedDateTime entityLastModified, int maxWidth, int maxHeight, FileType fileType) throws IOException {
        // Write image to a stream that discards all bytes, so we don't use more memory than necessary.
        LengthCountingOutputStream stream = new LengthCountingOutputStream();
        renderResized(source, hash, entityLastModified, stream, maxWidth, maxHeight, fileType);
        return stream.length();
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
