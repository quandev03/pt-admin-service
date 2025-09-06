package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import vn.vnsky.bcss.admin.annotation.UlidSequence;

import java.io.Serializable;
import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "object_action")
@FieldNameConstants
public class ObjectActionEntity implements Serializable {

    @Id
    @UlidSequence
    @Column(name = "ID")
    private String id;

    @Column(name = "OBJECT_ID")
    private String objectId;

    @Column(name = "ACTION_ID")
    private String actionId;

    @Column(name = "NAME")
    private String name;

    @Column(name = "STATUS")
    private Integer status;

    @ManyToMany(targetEntity = RoleEntity.class, mappedBy = "objectActions")
    @NotFound(action = NotFoundAction.IGNORE)
    @ToString.Exclude
    private List<RoleEntity> roles;

}
