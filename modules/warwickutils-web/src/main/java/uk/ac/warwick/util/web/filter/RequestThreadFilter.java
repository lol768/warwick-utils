package uk.ac.warwick.util.web.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Records request details against threads, so that when we are observing
 * the current running threads we can get some more detailed information about the
 * ones which are serving HTTP requests.
 */
public class RequestThreadFilter extends AbstractHttpFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestThreadFilter.class);
    
    private ConcurrentMap<Thread, RequestData> map = new ConcurrentHashMap<Thread, RequestData>();

    // Use an object pool to reduce the number of new objects we create on a request
    private ObjectPool<RequestData> dataPool = new GenericObjectPool<>(
        new RequestDataFactory(),
        new GenericObjectPoolConfig<RequestData>() {{
            setMaxIdle(100);
            setMinIdle(50);
            setLifo(true);
        }}
    );
    
    public void destroy() {}
    public void init(FilterConfig config) throws ServletException {
    }
    
    public RequestData get(Thread t) {
        return map.get(t);
    }
    
    public Map<Thread, RequestData> getAll() {
        return Collections.unmodifiableMap(map);
    }
    
    public void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        RequestData data = null;
        Thread thread = Thread.currentThread();
        try {
            String ip = (String) req.getAttribute(RequestIPAddressFilter.CURRENT_IP_STRING_ATTRIBUTE);
            if (ip == null) {
                ip = req.getRemoteHost();
            }
            data = borrowRequestData();
            data.init(req.getMethod(), ip, req.getRequestURL(), req.getQueryString());
            
            if (map.put(thread, data) != null) {
                // if this happens, we're either not removing from map after request, or Thread equality
                // is not working properly
                LOGGER.error("Filter is overwriting an existing Thread key!");
            }
            
            chain.doFilter(req, res);
        } finally {
            map.remove(thread);
            if (data != null) {
                try {
                    dataPool.returnObject(data);
                } catch (Exception e) {
                    LOGGER.error("Did not expect to fail returning object to pool", e);
                }
            }
        }
    }
    private RequestData borrowRequestData() {
        try {
            return (RequestData) dataPool.borrowObject();
        } catch (Exception e) {
            // we're only 
            LOGGER.error("Failed to get a RequestData", e);
            return null;
        }
    }
    
    public static final class RequestData {
        private StringBuffer requestURL;
        private String ip;
        private String query;
        private String method;
        private long createdTime;
        public void init(String method, String ip, StringBuffer url, String queryString) {
            this.method = method;
            this.requestURL = url;
            this.ip = ip;
            this.query = queryString;
            this.createdTime = System.currentTimeMillis();
        }
        /**
         * requestURL as in request.getRequestURL, ie the whole
         * URL excluding query strings
         */
        public String getRequestURL() {
            return requestURL.toString();
        }
        public String getIp() {
            return ip;
        }
        public String getQuery() {
            return query;
        }
        public String getMethod() {
            return method;
        }
        public long getCreatedTime() {
            return createdTime;
        }
    }
    
    final static class RequestDataFactory extends BasePooledObjectFactory<RequestData> {
        @Override
        public RequestData create() {
            return new RequestData();
        }

        @Override
        public PooledObject<RequestData> wrap(RequestData requestData) {
            return new DefaultPooledObject<>(requestData);
        }
    }

}
