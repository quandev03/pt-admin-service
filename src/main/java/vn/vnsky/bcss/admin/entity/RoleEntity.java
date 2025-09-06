package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.security.core.GrantedAuthority;
import vn.vnsky.bcss.admin.annotation.UlidSequence;

import java.io.Serial;
import java.util.List;

@ToString
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "\"ROLE\"")
public class RoleEntity extends AbstractAuditingEntity implements GrantedAuthority {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @UlidSequence
    @Column(name = "id")
    private String id;

    @Column(name = "app_id")
    private String appId;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private Integer status;

    @Transient
    private List<String> checkedKeys;

    @ManyToMany(targetEntity = ObjectActionEntity.class, cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "role_object_action",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = {
                    @JoinColumn(name = "object_id", referencedColumnName = "object_id"),
                    @JoinColumn(name = "action_id", referencedColumnName = "action_id")}
    )
    @NotFound(action = NotFoundAction.IGNORE)
    @ToString.Exclude
    private List<ObjectActionEntity> objectActions;

    public RoleEntity(String id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public RoleEntity(String id, String name, String code, Integer status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.status = status;
    }

    @Override
    public String getAuthority() {
        return this.code;
    }
}
