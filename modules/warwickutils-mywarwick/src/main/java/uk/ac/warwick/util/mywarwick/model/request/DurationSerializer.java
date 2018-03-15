package uk.ac.warwick.util.mywarwick.model.request;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Duration;

class DurationSerializer extends StdSerializer<Duration> {
    public DurationSerializer() { super(Duration.class); }
    @Override
    public void serialize(Duration duration, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {
        generator.writeNumber(duration.getSeconds());
    }
}
