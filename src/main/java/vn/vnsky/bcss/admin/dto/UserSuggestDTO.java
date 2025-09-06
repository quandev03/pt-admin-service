package vn.vnsky.bcss.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author thomas_luu
 * @created 11/04/2023 - 11:49 AM
 * @project admin-be
 */
@NoArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(value = {"enabled", "accountNonLocked", "accountNonExpired"})
public class UserSuggestDTO extends UserDTO {
}
