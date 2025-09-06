package vn.vnsky.bcss.admin.config.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import vn.vnsky.bcss.admin.config.auth.GoogleAuthenticationToken;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GoogleAuthenticationToken.class, name = "vn.vnsky.bcss.admin.config.auth.GoogleAuthenticationToken")
})
@JsonDeserialize(using = GoogleAuthenticationJsonDeserializer.class)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class GoogleAuthenticationMixin {
}
