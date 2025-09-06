package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import vn.vnsky.bcss.admin.annotation.UlidSequence;
import vn.vnsky.bcss.admin.constant.UserLoginMethod;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "\"USER\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedEntityGraph(name = "Users.graph",
        attributeNodes = {
                @NamedAttributeNode("groups"),
                @NamedAttributeNode("roles")
        }
)
@SecondaryTable(name = "DEPARTMENT_USER", pkJoinColumns = @PrimaryKeyJoinColumn(name = "USER_ID"))
public class UserEntity extends AbstractAuditingEntity {

    @Id
    @UlidSequence
    @Column(name = "id")
    private String id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "position_title")
    private String positionTitle;

    @Column(name = "type")
    private String type;

    @Builder.Default
    @Column(name = "login_method")
    private Integer loginMethod = UserLoginMethod.LOGIN_USERNAME.getValue();

    @Column(name = "login_failed_count")
    private Integer loginFailedCount;

    @Column(name = "password_expire_time")
    private LocalDateTime passwordExpireTime;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "status")
    private Integer status;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "group_user"
            , joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"))
    @NotFound(action = NotFoundAction.IGNORE)
    @OrderBy("id")
    private Set<GroupEntity> groups = new LinkedHashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "role_user"
            , joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    @NotFound(action = NotFoundAction.IGNORE)
    @OrderBy("id")
    private Set<RoleEntity> roles = new LinkedHashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "department_user"
            , joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "department_id", referencedColumnName = "id"))
    @NotFound(action = NotFoundAction.IGNORE)
    @OrderBy("id")
    private Set<DepartmentEntity> departments = new LinkedHashSet<>();

    @JoinColumn(name = "client_id")
    @ManyToOne(cascade = {CascadeType.MERGE})
    private ClientEntity client;

}
