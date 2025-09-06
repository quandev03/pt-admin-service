package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyCheckDTO {

    @JsonProperty(value = "app_code", access = JsonProperty.Access.WRITE_ONLY)
    private String appCode;

    @JsonProperty(value = "user_id", access = JsonProperty.Access.WRITE_ONLY)
    private String userId;

    @JsonProperty(value = "service_code", access = JsonProperty.Access.WRITE_ONLY)
    private String serviceCode;

    @JsonProperty(value = "uri_pattern", access = JsonProperty.Access.WRITE_ONLY)
    private String uriPattern;

    @JsonProperty(value = "method", access = JsonProperty.Access.WRITE_ONLY)
    private String method;

    @JsonProperty(value = "user", access = JsonProperty.Access.READ_ONLY)
    private UserDTO user;

    @JsonProperty(value = "granted", access = JsonProperty.Access.READ_ONLY)
    private Boolean granted;

    @JsonProperty(value = "matched_object_code", access = JsonProperty.Access.READ_ONLY)
    private String matchedObjectCode;

    @JsonProperty(value = "matched_action_code", access = JsonProperty.Access.READ_ONLY)
    private String matchedActionCode;

}
