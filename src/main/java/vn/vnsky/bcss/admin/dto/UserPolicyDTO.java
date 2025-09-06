package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPolicyDTO {

    @JsonProperty("app_code")
    private String appCode;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("resource_access")
    private Map<String, Set<String>> resourceAccess;

}
