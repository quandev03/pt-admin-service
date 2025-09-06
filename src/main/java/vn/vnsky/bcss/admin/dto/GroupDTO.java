package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.vnsky.bcss.admin.constant.ResponseView;
import vn.vnsky.bcss.admin.entity.GroupEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * A DTO for the {@link GroupEntity} entity.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupDTO extends AbstractAuditingDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1377938631734942986L;

    @Schema(description = "ID nhóm người dùng", example = "01GWRW8B12CX3SN0F9248VERDA", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    private String id;

    @Schema(description = "Mã nhóm người dùng", example = "code 1 ", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    @NotNull(message = "{error.message.group.code.blank}", groups = {CreateCase.class, UpdateCase.class})
    private String code;

    @NotNull(message = "{error.message.group.name.blank}", groups = {CreateCase.class, UpdateCase.class})
    @Schema(description = "Tên nhóm người dùng", example = "group 1 ", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    private String name;

    @JsonView({ResponseView.QuickSearch.class, ResponseView.Public.class})
    @Schema(description = "Trạng thái nhóm người dùng", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{error.message.group.status.blank}")
    private Integer status;

    @JsonView(ResponseView.QuickSearch.class)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Các vai trò thuộc nhóm người dùng này")
    private Set<RoleDTO> roles;

    @JsonView(ResponseView.QuickSearch.class)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Các người dùng thuộc nhóm người dùng này")
    private Set<UserDTO> users;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY, title = "Các id vai trò gán vào nhóm người dùng này")
    private Set<String> roleIds;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY, title = "Các id người dùng gán vào nhóm người dùng này")
    private Set<String> userIds;

    public interface CreateCase {
    }

    public interface UpdateCase {
    }
}
