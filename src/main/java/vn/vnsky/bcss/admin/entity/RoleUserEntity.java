package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RoleUserId.class)
public class RoleUserEntity {

    @Id
    @Column(name = "role_id")
    private String roleId;
    @Id
    @Column(name = "user_id")
    private String userId;
}
