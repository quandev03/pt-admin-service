package vn.vnsky.bcss.admin.config.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.data.web.PagedModel;

import java.io.IOException;
import java.util.Objects;

/**
 * @author thanhvt
 * @created 13/04/2023 - 11:36 CH
 * @project str-auth
 * @since 1.0
 **/
public class CustomPageSerializer extends StdSerializer<PagedModel<Object>> {

    private static JavaType constructType() {
        Class<?> containerClass = PagedModel.class;
        Class<?> elementType = Object.class;
        return TypeFactory.defaultInstance().constructParametricType(containerClass, elementType);
    }

    public CustomPageSerializer() {
        super(constructType());
    }

    @Override
    public void serialize(PagedModel value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("numberOfElements", value.getContent().size());
        PagedModel.PageMetadata pageMetadata = Objects.requireNonNull(value.getMetadata());
        gen.writeNumberField("number", pageMetadata.number());
        gen.writeNumberField("totalElements", pageMetadata.totalElements());
        gen.writeNumberField("totalPages", pageMetadata.totalPages());
        gen.writeNumberField("size", pageMetadata.size());
        gen.writeFieldName("content");
        provider.defaultSerializeValue(value.getContent(), gen);
        gen.writeEndObject();
    }

    // inherited constructor removed for concision
}
