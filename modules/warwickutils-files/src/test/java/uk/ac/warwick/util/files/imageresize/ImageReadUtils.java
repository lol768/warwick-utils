package uk.ac.warwick.util.files.imageresize;

import com.sun.media.jai.codec.ByteArraySeekableStream;

import javax.imageio.ImageIO;
import javax.media.jai.PlanarImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

class ImageReadUtils {
    private ImageReadUtils(){}

    static PlanarImage read(ByteArrayOutputStream output) throws IOException {
        return PlanarImage.wrapRenderedImage(ImageIO.read(new ByteArraySeekableStream(output.toByteArray())));
    }
}
