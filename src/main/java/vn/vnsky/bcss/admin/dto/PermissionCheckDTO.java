package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionCheckDTO {

    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<PermissionDTO> checkPermissions;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean allowed;

}
