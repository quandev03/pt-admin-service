package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import vn.vnsky.bcss.admin.annotation.UlidSequence;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A Groups.
 */
@Entity
@Table(name = "\"GROUP\"")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(name = "Groups.graph",
        attributeNodes = {
                @NamedAttributeNode("roles"),
                @NamedAttributeNode("users")
        }
)
public class GroupEntity extends AbstractAuditingEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @UlidSequence
    @Column(name = "id")
    private String id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    private Integer status;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "group_role"
            , joinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    @NotFound(action = NotFoundAction.IGNORE)
    @ToString.Exclude
    private Set<RoleEntity> roles = new LinkedHashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "group_user"
            , joinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    @NotFound(action = NotFoundAction.IGNORE)
    @ToString.Exclude
    private Set<UserEntity> users = new LinkedHashSet<>();

    @JoinColumn(name = "client_id")
    @ManyToOne(cascade = {CascadeType.MERGE})
    private ClientEntity client;

}
