package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import vn.vnsky.bcss.admin.annotation.UlidSequence;
import vn.vnsky.bcss.admin.constant.AuthConstants;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "object")
@SQLRestriction("status=" + AuthConstants.ModelStatus.ACTIVE)
public class ObjectEntity {

    @Id
    @UlidSequence
    private String id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "app_id")
    private String appId;

    @Column(name = "uri")
    private String url;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "status")
    private Integer status;

    @Column(name = "icon")
    private String icon;

    @Column(name = "ordinal")
    private Integer ordinal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private ObjectEntity object;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "object", targetEntity = ObjectEntity.class)
    private List<ObjectEntity> children;
}
