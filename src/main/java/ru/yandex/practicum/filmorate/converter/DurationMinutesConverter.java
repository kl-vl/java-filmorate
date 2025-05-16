package ru.yandex.practicum.filmorate.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Duration;

public class DurationMinutesConverter {
    public static class Serializer extends JsonSerializer<Duration> {
        @Override
        public void serialize(Duration value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            gen.writeNumber(value.toMinutes());
        }
    }

    public static class Deserializer extends JsonDeserializer<Duration> {
        @Override
        public Duration deserialize(JsonParser p, DeserializationContext ctx)
                throws IOException {
            long minutes = p.getLongValue();
            return Duration.ofMinutes(minutes);
        }
    }
}
