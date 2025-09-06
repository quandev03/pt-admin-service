package vn.vnsky.bcss.admin.config.json;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;

import java.sql.Timestamp;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Timestamp.class, name = "java.sql.Timestamp")
})
@JsonDeserialize(using = DateDeserializers.TimestampDeserializer.class)
public abstract class TimestampMixin {
}
