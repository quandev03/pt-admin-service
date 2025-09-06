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
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@JsonComponent
public class DateJsonComponent {

    private DateJsonComponent() {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DateSerializer extends JsonSerializer<Date>
            implements ContextualSerializer {

        private static final String DEFAULT_FORMAT = "yyyy-MM-dd";

        private static final Map<String, DateSerializer> FORMAT_SERIALIZER_MAP = new HashMap<>();

        private final SimpleDateFormat dateFormatter;

        public DateSerializer(String format) {
            this.dateFormatter = new SimpleDateFormat(format);
            FORMAT_SERIALIZER_MAP.put(format, this);
        }

        public DateSerializer() {
            this(DEFAULT_FORMAT);
        }

        @Override
        public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (date == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeString(dateFormatter.format(date));
            }
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
            AnnotatedMember annotatedMember = beanProperty.getMember();
            JsonFormat.Value value = serializerProvider.getAnnotationIntrospector().findFormat(annotatedMember);
            if (value != null && value.hasPattern()) {
                DateSerializer dateSerializer = FORMAT_SERIALIZER_MAP.get(value.getPattern());
                if (dateSerializer == null) {
                    dateSerializer = new DateSerializer(value.getPattern());
                    FORMAT_SERIALIZER_MAP.put(value.getPattern(), dateSerializer);
                }
                return dateSerializer;
            } else {
                return FORMAT_SERIALIZER_MAP.get(DEFAULT_FORMAT);
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DateDeserializer extends JsonDeserializer<Date> implements ContextualDeserializer {

        private static final String DEFAULT_FORMAT = "yyyy-MM-dd";

        private static final Map<String, DateDeserializer> FORMAT_DESERIALIZER_MAP = new HashMap<>();

        private final SimpleDateFormat dateFormatter;

        public DateDeserializer(String format) {
            this.dateFormatter = new SimpleDateFormat(format);
            FORMAT_DESERIALIZER_MAP.put(format, this);
        }

        public DateDeserializer() {
            this(DEFAULT_FORMAT);
        }

        @SneakyThrows
        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
            if (treeNode instanceof TextNode textNode) {
                java.util.Date date = dateFormatter.parse(textNode.textValue());
                Calendar calendarLocal = Calendar.getInstance();
                calendarLocal.setTime(date);
                Calendar calendarUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendarUtc.set(calendarLocal.get(Calendar.YEAR), calendarLocal.get(Calendar.MONTH), calendarLocal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                return new Date(calendarUtc.getTimeInMillis());
            }
            return null;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
            AnnotatedMember annotatedMember = beanProperty.getMember();
            JsonFormat.Value value = deserializationContext.getAnnotationIntrospector().findFormat(annotatedMember);
            if (value != null && value.hasPattern()) {
                DateDeserializer dateDeserializer = FORMAT_DESERIALIZER_MAP.get(value.getPattern());
                if (dateDeserializer == null) {
                    dateDeserializer = new DateDeserializer(value.getPattern());
                    FORMAT_DESERIALIZER_MAP.put(value.getPattern(), dateDeserializer);
                }
                return dateDeserializer;
            } else {
                return FORMAT_DESERIALIZER_MAP.get(DEFAULT_FORMAT);
            }
        }
    }
}
