package uk.ac.warwick.util.content.texttransformers.embed;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import uk.ac.warwick.util.web.Uri;

public class OEmbedRequest implements Serializable {

    private static final long serialVersionUID = -8273039402512567099L;
    
    private final Uri uri;
    
    private Integer maxWidth;
    
    private Integer maxHeight;
    
    public OEmbedRequest(Uri uri) {
        this.uri = uri;
    }

    public final Integer getMaxWidth() {
        return maxWidth;
    }

    public final void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
    }

    public final Integer getMaxHeight() {
        return maxHeight;
    }

    public final void setMaxHeight(Integer maxHeight) {
        this.maxHeight = maxHeight;
    }

    public final Uri getUri() {
        return uri;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().isAssignableFrom(OEmbedRequest.class)) { return false; }
        
        OEmbedRequest other = (OEmbedRequest) obj;
        return new EqualsBuilder()
               .append(uri, other.getUri())
               .append(maxWidth, other.getMaxWidth())
               .append(maxHeight, other.getMaxHeight())
               .build();      
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
               .append(uri)
               .append(maxWidth)
               .append(maxHeight)
               .build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
               .append("uri", uri)
               .append("maxWidth", maxWidth)
               .append("maxHeight", maxHeight)
               .build();
    }

}
