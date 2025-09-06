package vn.vnsky.bcss.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author thomas_luu
 * @created 03/04/2023 - 10:54 AM
 * @project str-auth
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoleUserId implements Serializable {

    private String roleId;

    private String userId;
}
