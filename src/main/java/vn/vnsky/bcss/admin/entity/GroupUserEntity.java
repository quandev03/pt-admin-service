package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "group_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(GroupUserId.class)
public class GroupUserEntity {

    @Id
    @Column(name = "user_id")
    private String userId;
    @Id
    @Column(name = "group_id")
    private String groupId;
}
