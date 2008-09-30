package uk.ac.warwick.util.content.texttransformers.media;

import java.util.regex.Pattern;

public final class GrouperMediaUrlHandler extends AbstractFlashPlayerMediaUrlHandler {
    
    private static final Pattern PATTERN = Pattern.compile("http://(?:www\\.)?grouper\\.com/video/MediaDetails.aspx\\?(.*id=.+)", Pattern.CASE_INSENSITIVE);
    
    public boolean recognises(final String url) {
        return PATTERN.matcher(url.toString()).matches();
    }

    @Override
    public String getFlashUrl(final String url) {
        String params = toURL(url).getQuery();
        String videoId = extractVideoId(params, "id");
        return "http://grouper.com/mtg/mtgPlayer.swf?v=1.7&vurl=http%3a%2f%2fgrouper.com%2frss%2fflv.ashx%3fid%3d" + videoId + "%26rf%3d-1&vfver=8&ap=0&extid=-1";
    }
}