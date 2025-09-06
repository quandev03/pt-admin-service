package vn.vnsky.bcss.admin.config.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Long.class, name = "java.lang.Long")
})
@JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
public abstract class LongMixin {
}
