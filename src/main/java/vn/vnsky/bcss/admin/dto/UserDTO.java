package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import vn.vnsky.bcss.admin.constant.ResponseView;
import vn.vnsky.bcss.admin.constant.UserLoginMethod;
import vn.vnsky.bcss.admin.constant.UserStatus;
import vn.vnsky.bcss.admin.entity.UserEntity;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.*;

/**
 * A DTO for the {@link UserEntity} entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(value = {"name", "attributes", "locked"}, allowGetters = true)
@FieldNameConstants
public class UserDTO extends AbstractAuditingDTO implements UserDetails, OAuth2AuthenticatedPrincipal {

    @Serial
    private static final long serialVersionUID = -4881046053049599618L;

    @JsonView({ResponseView.GroupUserSuggest.class, ResponseView.QuickSearch.class, ResponseView.Public.class})
    @Schema(title = "ID người dùng", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @JsonView({ResponseView.GroupUserSuggest.class, ResponseView.QuickSearch.class, ResponseView.Public.class})
    @Schema(title = "Tên đăng nhập người dùng", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{error.message.user.username.not-empty}", groups = {CreateCase.class})
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
//    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[~@#$!%^*?&()])(?=\\S+$).{6,}$", groups = {CreateCase.class})
    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY, title = "Mật khẩu người dùng", pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\\\d)(?=.*[~@#$!%^*?&()])(?=\\\\S+$).{6,}$")
    private String password;

    @NotBlank(message = "{error.message.user.fullname-not-empty}", groups = {CreateCase.class, UpdateCase.class, UpdateProfileCase.class})
    @JsonView({ResponseView.GroupUserSuggest.class, ResponseView.QuickSearch.class, ResponseView.Public.class})
    @Schema(title = "Họ tên người dùng", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullname;

    @JsonView({ResponseView.Public.class})
    @Schema(title = "Ngày sinh người dùng")
    private Date dateOfBirth;

    @JsonView({ResponseView.Public.class})
    @Schema(title = "Chức vụ người dùng")
    private String positionTitle;

    @NotNull(message = "{error.message.user.status-not-null}", groups = {CreateCase.class, UpdateCase.class})
    @JsonView({ResponseView.GroupUserSuggest.class, ResponseView.QuickSearch.class, ResponseView.Public.class})
    @Schema(title = "Trạng thái người dùng", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @JsonView({ResponseView.Public.class})
    private String type;

    @Builder.Default
    @JsonView({ResponseView.Public.class})
    private Integer loginMethod = UserLoginMethod.LOGIN_USERNAME.getValue();

    @NotBlank(message = "{error.message.user.email-not-empty}", groups = {CreateCase.class})
    @JsonView({ResponseView.GroupUserSuggest.class, ResponseView.QuickSearch.class, ResponseView.Public.class})
    @Pattern(message = "{error.message.user.email.not-valid}", regexp = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$", groups = {CreateCase.class})
    @Schema(title = "Địa chỉ thư điện tử người dùng", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @JsonView({ResponseView.GroupUserSuggest.class, ResponseView.QuickSearch.class, ResponseView.Public.class})
    @Schema(title = "Số điện thoại người dùng")
    private String phoneNumber;

    @JsonView({ResponseView.Public.class})
    @Schema(title = "Giới tính người dùng")
    private Integer gender;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Mật khẩu cần phải đổi")
    private boolean needChangePassword = false;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Thời gian hết hạn mật khẩu")
    private LocalDateTime passwordExpireTime;

    @JsonIgnore
    @Schema(hidden = true)
    private Integer loginFailedCount;

    @JsonView({ResponseView.GroupUserSuggest.class, ResponseView.QuickSearch.class, ResponseView.Public.class})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Các nhóm người dùng mà người dùng này thuộc về")
    private Set<GroupDTO> groups;

    @JsonView({ResponseView.GroupUserSuggest.class, ResponseView.QuickSearch.class, ResponseView.Public.class})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Các vai trò mà người dùng này thuộc về")
    private Set<RoleDTO> roles;

    @JsonView({ResponseView.GroupUserSuggest.class, ResponseView.QuickSearch.class, ResponseView.Public.class})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Các phòng ban mà người dùng này thuộc về")
    private Set<DepartmentDTO> departments;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY, title = "Các id nhóm người dùng gán cho người dùng này")
    private Set<String> groupIds;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY, title = "Các id vai trò gán cho người dùng này")
    private Set<String> roleIds;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY, title = "Các id phòng ban gán cho người dùng này")
    private Set<Long> departmentIds;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(accessMode = Schema.AccessMode.WRITE_ONLY, title = "Các id kho gán cho người dùng này")
    private Set<Long> stockIds;

    @JsonView({ResponseView.Public.class})
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "The client which this user belong to")
    private ClientDTO client;

    @JsonView({ResponseView.GroupUserSuggest.class, ResponseView.QuickSearch.class, ResponseView.Public.class})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, title = "Global qualified username")
    private String preferredUsername;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return (this.roles == null || this.roles.isEmpty()) ? Collections.singleton(new SimpleGrantedAuthority("SCOPE_DEFAULT")) : this.roles;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !Objects.equals(this.status, UserStatus.LOCKED.getValue());
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return this.passwordExpireTime == null || this.passwordExpireTime.isAfter(LocalDateTime.now());
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return Objects.equals(this.status, UserStatus.ACTIVE.getValue());
    }

    @Override
    public String getName() {
        return this.username;
    }

    @JsonProperty
    @Schema(hidden = true)
    private final Map<String, Object> attributes = new HashMap<>();

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public interface CreateCase {
    }

    public interface UpdateCase {
    }

    public interface UpdateProfileCase {
    }

}
