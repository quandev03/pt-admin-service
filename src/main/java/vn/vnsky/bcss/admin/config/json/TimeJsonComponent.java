package vn.vnsky.bcss.admin.config.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.*;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;

@JsonComponent
public class TimeJsonComponent {

    private TimeJsonComponent() {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TimeSerializer extends JsonSerializer<Time> implements ContextualSerializer {

        private static final String DEFAULT_FORMAT = "HH:mm:ss";

        private static final Map<String, TimeSerializer> FORMAT_SERIALIZER_MAP = new HashMap<>();

        private final SimpleDateFormat timeFormatter;

        public TimeSerializer(String format) {
            this.timeFormatter = new SimpleDateFormat(DEFAULT_FORMAT);
            FORMAT_SERIALIZER_MAP.put(format, this);
        }

        public TimeSerializer() {
            this(DEFAULT_FORMAT);
        }

        @Override
        public void serialize(Time time, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (time == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeString(timeFormatter.format(time));
            }
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
            AnnotatedMember annotatedMember = beanProperty.getMember();
            JsonFormat.Value value = serializerProvider.getAnnotationIntrospector().findFormat(annotatedMember);
            if (value != null && value.hasPattern()) {
                TimeSerializer timeSerializer = FORMAT_SERIALIZER_MAP.get(value.getPattern());
                if (timeSerializer == null) {
                    timeSerializer = new TimeSerializer(value.getPattern());
                    FORMAT_SERIALIZER_MAP.put(value.getPattern(), timeSerializer);
                }
                return timeSerializer;
            } else {
                return FORMAT_SERIALIZER_MAP.get(DEFAULT_FORMAT);
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TimeDeserializer extends JsonDeserializer<Time> implements ContextualDeserializer {

        private static final String DEFAULT_FORMAT = "HH:mm:ss";

        private static final Map<String, TimeDeserializer> FORMAT_DESERIALIZER_MAP = new HashMap<>();

        private final SimpleDateFormat timeFormatter;

        public TimeDeserializer(String format) {
            this.timeFormatter = new SimpleDateFormat(format);
            FORMAT_DESERIALIZER_MAP.put(format, this);
        }

        public TimeDeserializer() {
            this(DEFAULT_FORMAT);
        }

        @SneakyThrows
        @Override
        public Time deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
            if (treeNode instanceof TextNode textNode) {
                Date date = timeFormatter.parse(textNode.textValue());
                Calendar calendarLocal = Calendar.getInstance();
                calendarLocal.setTime(date);
                Calendar calendarUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendarUtc.set(1970, Calendar.JANUARY, 1, calendarLocal.get(Calendar.HOUR_OF_DAY), calendarLocal.get(Calendar.MINUTE), calendarLocal.get(Calendar.SECOND));
                return new Time(calendarUtc.getTimeInMillis());
            }
            return null;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
            AnnotatedMember annotatedMember = beanProperty.getMember();
            JsonFormat.Value value = deserializationContext.getAnnotationIntrospector().findFormat(annotatedMember);
            if (value != null && value.hasPattern()) {
                TimeDeserializer timeDeserializer = FORMAT_DESERIALIZER_MAP.get(value.getPattern());
                if (timeDeserializer == null) {
                    timeDeserializer = new TimeDeserializer(value.getPattern());
                    FORMAT_DESERIALIZER_MAP.put(value.getPattern(), timeDeserializer);
                }
                return timeDeserializer;
            } else {
                return FORMAT_DESERIALIZER_MAP.get(DEFAULT_FORMAT);
            }
        }
    }
}
