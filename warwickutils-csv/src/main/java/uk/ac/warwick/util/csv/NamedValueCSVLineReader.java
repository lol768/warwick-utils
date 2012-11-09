package uk.ac.warwick.util.csv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of CSVLineReader which just puts everything into a big bunch of lists.
 * @author Nick Howes
 */
public final class NamedValueCSVLineReader implements CSVLineReader<List<String>> {

    private List<String> headers;
    private List<Map<String,String>> data = new ArrayList<Map<String,String>>();
    
    private boolean hasHeaders;
    private boolean firstLine = true;
    
    private int tableWidth;
    
    public List<String> constructNewObject() {
        return new ArrayList<String>();
    }

    public void end(final List<String> line) {
        tableWidth = Math.max(line.size(), tableWidth);
        if (hasHeaders && firstLine) {
            headers = line;
        } else {
            data.add(toMap(line));
        }
        firstLine = false;
    }

    /**
     * Convert a list of values to a map with header names as keys,
     * or string numbers if there are no headers.
     */
    private Map<String, String> toMap(List<String> line) {
        Map<String,String> map = new HashMap<String, String>();
        for (int i=0; i<line.size(); i++) {
            String key = (hasHeaders && i < headers.size()) ? headers.get(i) : (""+i);
            key = resolveDuplicateKeys(key, map);
            map.put(key, line.get(i));
        }        
        return map;
    }

    /**
     * Resolve problem of duplicate header names, by sticking a number on the end of it.
     */
    private String resolveDuplicateKeys(final String tkey, final Map<String, String> map) {
        String key = tkey;
        String keyCandidate = key;
        DuplicateResolver<String> resolver = DuplicateResolver.incrementingNumber();
        while (map.containsKey(keyCandidate)) {
            keyCandidate = resolver.getAlternate(key);
        }
        key = keyCandidate;
        return key;
    }

    public void setColumn(final List<String> line, final int col, final String text) {
        if (col != line.size()) {
            throw new IllegalArgumentException("setColumn is being called out of order");
        }
        line.add(col, text);
    }

    public boolean isHasHeaders() {
        return hasHeaders;
    }

    public void setHasHeaders(boolean hasHeaders) {
        this.hasHeaders = hasHeaders;
    }

    public int getTableWidth() {
        return tableWidth;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<Map<String, String>> getData() {
        return data;
    }

    public void endData() {
        // TODO Auto-generated method stub
        
    }

}
