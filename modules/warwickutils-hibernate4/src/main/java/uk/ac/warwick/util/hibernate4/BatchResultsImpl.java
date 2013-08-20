package uk.ac.warwick.util.hibernate4;

import java.util.Set;

import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.util.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

/**
 * A wrapper onto {@link ScrollableResults} which enforces strong typing and
 * takes a callback to perform an operation. Manages memory effectively within a
 * given batch size.
 */
public final class BatchResultsImpl<T> implements BatchResults<T> {
    
    private final Set<String> idents = Sets.newHashSet();

    private final ScrollableResults delegate;
    
    private final int batchSize;
    
    private final Function<? super T, String> identifier;
    
    private final Session session;
    
    public BatchResultsImpl(ScrollableResults theDelegate, int theBatchSize, Function<? super T, String> idFunction, Session theSession) {
        this.delegate = theDelegate;
        this.identifier = idFunction;
        this.batchSize = theBatchSize;
        this.session = theSession;
    }

    public void doWithBatch(Callback<T> callback) throws Exception {
        try {
            int i = 0;
            while (delegate.next()) {
                @SuppressWarnings("unchecked")
                T entity = (T) delegate.get(0);
                String id = identifier.apply(entity);
                
                if (!StringUtils.hasText(id)) {
                    throw new Exception("Entity did not return a valid unique ID");
                }
                
                if (idents.add(id)) {
                    callback.run(entity);
                }
                
                if (++i > batchSize) {
                    session.clear();
                    i = 0;
                }
            }
        } finally {
            delegate.close();
        }
    }
    
}
