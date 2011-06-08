package uk.ac.warwick.util.content.texttransformers.media;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.google.common.collect.Maps;

public final class PreziMediaUrlHandler extends MediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("https?://(?:.*\\.)?prezi\\.com/([^/]+).*", Pattern.CASE_INSENSITIVE);
    
    public boolean recognises(final String url) {
        return PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getHtml(String url, Map<String, Object> parameters) {
        Matcher m = PATTERN.matcher(url);
        Assert.isTrue(m.matches());
        String preziId = m.group(1);
        
        // defaults
        if (!parameters.containsKey("locktopath")) {
            parameters.put("locktopath", "false");
        }
        
        Map<String, Object> model = Maps.newHashMap();
        model.put("id", preziId);
        
        model.putAll(parameters);
        return renderTemplate("media/prezi.ftl", model);
    }

}
