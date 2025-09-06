package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.constant.ResponseView;
import vn.vnsky.bcss.admin.entity.RoleEntity;
import vn.vnsky.bcss.admin.util.DbColumnMapper;

import java.io.Serial;
import java.util.List;

/**
 * A DTO for the {@link RoleEntity} entity.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleDTO extends AbstractAuditingDTO implements GrantedAuthority {

    @Serial
    private static final long serialVersionUID = -7141948579337327129L;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    @Schema(title = "ID vai trò", accessMode = Schema.AccessMode.READ_ONLY)
    @DbColumnMapper("id")
    private String id;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    @Schema(title = "Mã vai trò", requiredMode = Schema.RequiredMode.REQUIRED)
    @DbColumnMapper("code")
    @Size(max = 20, message = "{error.message.role.role-code.invalid}")
    @NotBlank(message = "{error.message.role.role-code.not-blank}")
    @Pattern(regexp = AuthConstants.ValidationUserField.NON_VIETNAMESE_CHARACTER, message = "{error.message.role.role-code.invalid}")
    @Pattern(regexp = AuthConstants.ValidationUserField.NON_SPACE, message = "{error.message.role.role-code.invalid}")
    private String code;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    @Schema(title = "Tên vai trò", requiredMode = Schema.RequiredMode.REQUIRED)
    @DbColumnMapper("name")
    @Size(max = 100, message = "{error.message.role.role-name.invalid}")
    @NotBlank(message = "{error.message.role.role-name.not-blank}")
    private String name;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    @Schema(title = "Trạng thái vai trò", requiredMode = Schema.RequiredMode.REQUIRED, maximum = "1", minimum = "0")
    @DbColumnMapper("status")
    @NotNull(message = "{error.message.role.status.not-null}")
    private Integer status;

    @JsonView(ResponseView.QuickSearch.class)
    @Schema(title = "Mô tả vai trò")
    @DbColumnMapper("description")
    private String description;

    @Schema(title = "Danh sách ánh xạ ID chức năng - ID hành động mà người dùng với vai trò này được phép thao tác")
    private List<String> checkedKeys;

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getAuthority() {
        return this.code;
    }

}
