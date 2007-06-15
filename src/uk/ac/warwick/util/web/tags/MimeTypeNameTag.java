package uk.ac.warwick.util.web.tags;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Tag which renders a friendly name for a given MIME type.
 *
 * @author cusebr
 */
public final class MimeTypeNameTag extends TagSupport {
    private static final long serialVersionUID = -5219943614407001433L;
    private static HashMap<String, String> mimeNames;
    
    private String mimeType;
    
    public MimeTypeNameTag() {
        /*
         * I feel that this could be somehow moved into some nice
         * XML. But am not sure how that would be done with tags.
         * - nickh
         */
        if (mimeNames == null) {
            mimeNames = new HashMap<String, String>();
            mimeNames.put("application/pdf", "Adobe PDF document");
            mimeNames.put("application/msword", "Word document");
            mimeNames.put("application/vnd.ms-excel", "Excel spreadsheet");
            mimeNames.put("application/vnd.ms-powerpoint", "Powerpoint presentation");
            mimeNames.put("application/vnd.visio", "MS Visio File");
            mimeNames.put("application/vnd.ms-project", "MS Project File");
            mimeNames.put("application/x-project", "Project File");
            mimeNames.put("application/postscript", "PostScript File");
            mimeNames.put("application/x-dvi", "DVI File");
            mimeNames.put("text/plain", "Text file");
            mimeNames.put("text/html", "HTML page");
            mimeNames.put("application/rtf", "Rich Text document");
            mimeNames.put("image/gif", "GIF image");
            mimeNames.put("image/jpg", "JPEG image");
            mimeNames.put("image/jpeg", "JPEG image");
            mimeNames.put("image/pjpeg", "JPEG image");
            mimeNames.put("image/png", "PNG image");
            
            mimeNames.put("application/xml", "XML data");
            mimeNames.put("application/mac-binary", "Mac binary");
            
            mimeNames.put("application/futuresplash", "Shockwave Flash animation");
            mimeNames.put("application/x-shockwave-flash", "Shockwave Flash animation");
            mimeNames.put("application/x-director", "Director movie");
            
            mimeNames.put("audio/mpeg3", "MP3 sound clip");
            mimeNames.put("audio/wav", "WAV sound clip");
            mimeNames.put("audio/mpeg", "Sound clip");
            mimeNames.put("audio/basic", "Sound clip");
            mimeNames.put("audio/midi", "MIDI music clip");
            mimeNames.put("audio/s3m", "ScreamTracker music file");
            mimeNames.put("audio/mod", "MOD music file");
            
            mimeNames.put("application/zip", "Zipped archive");
            mimeNames.put("application/x-gzip", "GZipped archive");
            
            String videoClipString = "Video clip";            
            mimeNames.put("video/animaflex",videoClipString);
            mimeNames.put("video/x-ms-asf",videoClipString);
            mimeNames.put("application/x-troff-msvideo",videoClipString);
            mimeNames.put("video/avs-video",videoClipString);
            mimeNames.put("video/x-dv",videoClipString);
            mimeNames.put("video/fli",videoClipString);
            mimeNames.put("video/x-isvideo",videoClipString);
            mimeNames.put("video/mpeg",videoClipString);
            mimeNames.put("video/x-motion-jpeg",videoClipString);
            mimeNames.put("video/quicktime",videoClipString);
            mimeNames.put("video/x-sgi-movie",videoClipString);
            mimeNames.put("video/vnd.rn-realvideo",videoClipString);
        }
    }

    public int doStartTag() throws JspException {
        String mimeTypeName = getMimeTypeName();

        try {
            pageContext.getOut().write(mimeTypeName);
        } catch (final IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }


    String getMimeTypeName() {
        String mimeTypeName = mimeNames.get(this.mimeType);
        if (mimeTypeName == null) {
            mimeTypeName = this.mimeType;
        }
        return mimeTypeName;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }
}
