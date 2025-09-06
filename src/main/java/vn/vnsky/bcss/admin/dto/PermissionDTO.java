package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.vnsky.bcss.admin.util.DbColumnMapper;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionDTO {

    @DbColumnMapper("OBJECT_CODE")
    private String objectCode;

    @DbColumnMapper("ACTION_CODE")
    private String actionCode;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Set<String> actions;

}
