package vn.vnsky.bcss.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author thomas_luu
 * @created 03/04/2023 - 11:01 AM
 * @project str-auth
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GroupRoleId implements Serializable {

    private String roleId;

    private String groupId;
}
