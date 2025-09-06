package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "param")
@Getter
@Setter
public class ParamEntity {

    @Id
    private String id;

    @Column
    private String code;

    @Column
    private String value;

    @Column
    private String type;

    @Column
    private Integer status;

    @Column
    private String appCode;

    @Column
    private String description;

    @Column
    private String translations;
}
