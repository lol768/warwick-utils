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
        if (mimeNames == null) {
            mimeNames = new HashMap<String, String>();
            mimeNames.put("application/pdf", "Adobe PDF document");
            mimeNames.put("application/msword", "Word document");
            mimeNames.put("application/vnd.ms-excel", "Excel spreadsheet");
            mimeNames.put("application/vnd.ms-powerpoint", "Powerpoint presentation");
            mimeNames.put("application/vnd.visio", "MS Visio file");
            mimeNames.put("application/vnd.ms-project", "MS Project file");
            mimeNames.put("application/x-project", "Project file");
            mimeNames.put("application/postscript", "PostScript file");
            mimeNames.put("application/x-dvi", "DVI file");
            mimeNames.put("application/x-msaccess", "Access database");
            mimeNames.put("application/json", "JSON file");
            mimeNames.put("text/plain", "Text file");
            mimeNames.put("text/html", "HTML page");
            mimeNames.put("application/rtf", "Rich Text document");
            mimeNames.put("image/gif", "GIF image");
            mimeNames.put("image/jpg", "JPEG image");
            mimeNames.put("image/jpeg", "JPEG image");
            mimeNames.put("image/pjpeg", "JPEG image");
            mimeNames.put("image/png", "PNG image");
            mimeNames.put("image/bmp", "BMP image");
            mimeNames.put("image/tiff", "TIFF image");
            mimeNames.put("image/svg+xml", "SVG image");
            mimeNames.put("image/x-icon", "Icon image");
            mimeNames.put("application/x-javascript", "Javascript file");
            mimeNames.put("application/vnd.ms-fontobject", "Embedded OpenType font");
            mimeNames.put("application/x-font-truetype", "TrueType font");

            mimeNames.put("application/xml", "XML data");
            mimeNames.put("application/mac-binary", "Mac binary");

            mimeNames.put("application/futuresplash", "Shockwave Flash animation");
            mimeNames.put("application/x-shockwave-flash", "Shockwave Flash animation");
            mimeNames.put("application/x-director", "Director movie");
            
            mimeNames.put("application/zip", "Zipped archive");
            mimeNames.put("application/x-gzip", "GZipped archive");
            mimeNames.put("application/x-compressed", "Compressed archive");
            
            // specific audio
            mimeNames.put("audio/mpeg", "MP3 sound clip");
            mimeNames.put("audio/mpeg3", "MP3 sound clip");
            mimeNames.put("audio/wav", "WAV sound clip");
            mimeNames.put("audio/mpeg", "Sound clip");
            mimeNames.put("audio/basic", "Sound clip");
            mimeNames.put("audio/midi", "MIDI music clip");
            mimeNames.put("audio/s3m", "ScreamTracker music file");
            mimeNames.put("audio/mod", "MOD music file");
            
            mimeNames.put("text/csv", "Comma-separated values file");
            mimeNames.put("text/php", "PHP script");
            
            // non-specific audio
            String audioClipString = "Audio clip";
            mimeNames.put("audio/x-m4a", audioClipString);
            mimeNames.put("audio/x-ms-wma", audioClipString);
            mimeNames.put("audio/mp4", audioClipString);
            mimeNames.put("audio/ogg", audioClipString);
            mimeNames.put("audio/flac", audioClipString);

            String videoClipString = "Video clip";
            mimeNames.put("video/animaflex", videoClipString);
            mimeNames.put("video/x-ms-asf", videoClipString);
            mimeNames.put("application/x-troff-msvideo", videoClipString);
            mimeNames.put("video/avs-video", videoClipString);
            mimeNames.put("video/x-dv", videoClipString);
            mimeNames.put("video/fli", videoClipString);
            mimeNames.put("video/x-isvideo", videoClipString);
            mimeNames.put("video/mpeg", videoClipString);
            mimeNames.put("video/x-motion-jpeg", videoClipString);
            mimeNames.put("video/quicktime", videoClipString);
            mimeNames.put("video/x-sgi-movie", videoClipString);
            mimeNames.put("video/x-la-asf", videoClipString);
            mimeNames.put("video/vnd.rn-realvideo", videoClipString);
            
            mimeNames.put("video/x-vrml", videoClipString);
            mimeNames.put("video/x-msvideo", videoClipString);
            mimeNames.put("video/mp4", videoClipString);
            mimeNames.put("video/x-flv", videoClipString);
            mimeNames.put("video/x-m4v", videoClipString);
            mimeNames.put("video/x-ms-wmv", videoClipString);
            mimeNames.put("video/ogg", videoClipString);
            mimeNames.put("video/webm", videoClipString);
            mimeNames.put("video/3gpp", "Mobile video clip");
            
            mimeNames.put("application/ogg", "OGG application");
            mimeNames.put("application/x-java-jnlp-file", "Java Web Start definition");
            mimeNames.put("application/x-iphone", "iPhone application");
            mimeNames.put("text/css", "CSS style definitions");

            // Office 2007
            mimeNames.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Word document");
            mimeNames.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "Powerpoint presentation");
            mimeNames.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Excel spreadsheet");
            mimeNames.put("application/vnd.ms-word.document.macroEnabled.12", "Word document");
            mimeNames.put("application/vnd.ms-word.template.macroEnabled.12", "Word template");
            mimeNames.put("application/vnd.openxmlformats-officedocument.wordprocessingml.template", "Word template");
            mimeNames.put("application/vnd.ms-powerpoint.slideshow.macroEnabled.12", "Powerpoint presentation");
            mimeNames.put("application/vnd.openxmlformats-officedocument.presentationml.slideshow", "Powerpoint presentation");
            mimeNames.put("application/vnd.ms-powerpoint.presentation.macroEnabled.12", "Powerpoint presentation");
            mimeNames.put("application/vnd.ms-excel.sheet.binary.macroEnabled.12", "Excel spreadsheet");
            mimeNames.put("application/vnd.ms-excel.sheet.macroEnabled.12", "Excel spreadsheet");
            mimeNames.put("application/vnd.ms-xpsdocument", "XPS document");
            
            // Lecture recordings
            mimeNames.put("application/x-compressedlecture", "Recorded lecture");
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
