package vn.vnsky.bcss.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author thomas_luu
 * @created 03/04/2023 - 10:35 AM
 * @project str-auth
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GroupUserId implements Serializable {

    private String userId;

    private String groupId;

    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof GroupUserId groupUserId)) {
            return false;
        }
        return Objects.equals(groupUserId.userId, userId) &&
               Objects.equals(groupUserId.groupId, groupId);
    }
}
