package vn.vnsky.bcss.admin.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@JsonComponent
public class LocalDateTimeJsonComponent {

    public static final DateTimeFormatter ISO_FMT_INPUT = DateTimeFormatter.ISO_DATE_TIME;
    public static final DateTimeFormatter ISO_FMT_OUTPUT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private LocalDateTimeJsonComponent() {
    }

    public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (localDateTime == null) {
                jsonGenerator.writeNull();
            } else {
                jsonGenerator.writeString(localDateTime.atZone(ZoneId.systemDefault()).format(ISO_FMT_OUTPUT));
            }
        }
    }

    public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
            if (treeNode instanceof TextNode textNode) {
                return LocalDateTime.parse(textNode.textValue(), ISO_FMT_INPUT);
            }
            return null;
        }
    }
}
