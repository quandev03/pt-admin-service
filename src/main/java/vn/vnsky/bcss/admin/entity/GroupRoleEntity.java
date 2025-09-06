package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "group_role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(GroupRoleId.class)
public class GroupRoleEntity {

    @Id
    @Column(name = "role_id")
    private String roleId;

    @Id
    @Column(name = "group_id")
    private String groupId;
}
