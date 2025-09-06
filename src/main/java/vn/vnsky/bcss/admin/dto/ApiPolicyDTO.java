package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiPolicyDTO {

    @JsonProperty("service_code")
    private String serviceCode;

    @JsonProperty("api_uri_pattern")
    private String apiUriPattern;

    @JsonProperty("api_method")
    private String apiMethod;

    @JsonProperty(value = "is_ignore")
    private boolean isIgnore = false;

    @JsonProperty(value = "permissions")
    private List<PermissionDTO> permissions;

    @JsonSetter("uriPattern")
    public void setUriPattern(String value) {
        this.apiUriPattern = value;
    }

    @JsonSetter("method")
    public void setMethod(String value) {
        this.apiMethod = value;
    }

}
