package vn.vnsky.bcss.admin.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLRestriction;
import vn.vnsky.bcss.admin.annotation.UlidSequence;
import vn.vnsky.bcss.admin.constant.AuthConstants;

@Getter
@Setter
@ToString
@Entity
@Table(name = "action")
@SQLRestriction("status=" + AuthConstants.ModelStatus.ACTIVE)
public class ActionEntity {

    @Id
    @UlidSequence
    @Column
    private String id;

    @Column
    private String name;

    @Column
    private String code;

    private Integer status;
}
