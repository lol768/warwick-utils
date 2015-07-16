package uk.ac.warwick.util.web.spring.view.json;

import java.io.OutputStreamWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class JSON<T> {

    public abstract void write(OutputStreamWriter writer) throws JSONException;
    
    public abstract T unwrap();
    
    public static JSON<JSONObject> wrap(final JSONObject obj) {
        return new JSON<JSONObject>() {
            @Override
            public void write(OutputStreamWriter writer) throws JSONException {
                obj.write(writer);                
            }

            @Override
            public JSONObject unwrap() {
                return obj;
            }
        };
    }
    
    public static JSON<JSONArray> wrap(final JSONArray array) {
        return new JSON<JSONArray>() {
            @Override
            public void write(OutputStreamWriter writer) throws JSONException {
                array.write(writer);                
            }

            @Override
            public JSONArray unwrap() {
                return array;
            }
        };
    }

}
