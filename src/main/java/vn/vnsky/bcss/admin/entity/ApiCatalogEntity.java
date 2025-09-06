package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import vn.vnsky.bcss.admin.annotation.UlidSequence;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "API_CATALOG")
@FieldNameConstants
public class ApiCatalogEntity {

    @Id
    @UlidSequence
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SERVICE_CODE")
    private String serviceCode;

    @Column(name = "URI_PATTERN")
    private String uriPattern;

    @Column(name = "METHOD")
    private String method;

    @Column(name = "STATUS")
    private Integer status;

}
