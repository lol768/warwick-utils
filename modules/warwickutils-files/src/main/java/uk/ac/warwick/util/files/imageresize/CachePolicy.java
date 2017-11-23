package uk.ac.warwick.util.files.imageresize;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.List;

public final class CachePolicy {
    
    public static final Duration IMAGE_CACHE_PERIOD = Duration.ofHours(2);
    
    public static final CachePolicy PUBLIC_IMAGES = CachePolicy.publicCaching(IMAGE_CACHE_PERIOD);
    
    // We have to express this in days because otherwise Joda-Time won't run conversions on it (because of variable-length years)
    public static final Duration SITE_ASSET_CACHE_PERIOD = Duration.ofDays(365);
    
    public static final CachePolicy SITE_ASSET = CachePolicy.publicCaching(SITE_ASSET_CACHE_PERIOD);
    
    public static final CachePolicy PUBLIC = CachePolicy.publicCaching();
    
    public static final CachePolicy PRIVATE = CachePolicy.privateCaching();
    
    public static final CachePolicy NO_POLICY = CachePolicy.noPolicy();
    
    private enum Privacy {
        Public("public"),
        Private("private");
        
        private String cacheControlString;
        
        Privacy(String ccs) {
            this.cacheControlString = ccs;
        }
    }
    
    private final Privacy privacy;
    
    private final Duration expiresPeriod;
    
    private final boolean noCache;
    
    private CachePolicy(Privacy privacyPolicy, Duration expiry, boolean isNoCache) {
        this.privacy = privacyPolicy;
        this.expiresPeriod = expiry;
        this.noCache = isNoCache;
    }
    
    public Privacy getPrivacy() {
        return privacy;
    }

    public Duration getExpiresPeriod() {
        return expiresPeriod;
    }

    public boolean isNoCache() {
        return noCache;
    }
    
    private static CachePolicy publicCaching() {
        return publicCaching(null);
    }
    
    private static CachePolicy publicCaching(Duration expires) {
        return publicCaching(expires, false);
    }
    
    private static CachePolicy publicCaching(Duration expires, boolean noCache) {
        return new CachePolicy(Privacy.Public, expires, noCache);
    }
    
    private static CachePolicy privateCaching() {
        return privateCaching(null);
    }
    
    private static CachePolicy privateCaching(Duration expires) {
        return privateCaching(expires, false);
    }
    
    private static CachePolicy privateCaching(Duration expires, boolean noCache) {
        return new CachePolicy(Privacy.Private, expires, noCache);
    }
    
    private static CachePolicy noPolicy() {
        return new CachePolicy(null, null, false);
    }

    @Override
    public String toString() {
        List<String> components = Lists.newArrayList();
        
        if (noCache) {
            components.add("no-cache");
        }
        
        if (privacy != null) {
            components.add(privacy.cacheControlString);
        }
        
        if (expiresPeriod != null) {
            components.add("max-age=" + expiresPeriod.getSeconds());
            components.add("stale-while-revalidate=60");
        } else if (privacy != null) {
            components.add("max-age=0");
        }
        
        return StringUtils.join(components, ", ");
    }

}
