package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "API_CATALOG_ACL")
@IdClass(ApiCatalogAclEntity.ApiCatalogAclKey.class)
public class ApiCatalogAclEntity {

    @Id
    @Column(name = "CATALOG_ID")
    private String catalogId;

    @Id
    @Column(name = "ACL_ID")
    private String aclID;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApiCatalogAclKey implements Serializable {
        private String catalogId;
        private String aclID;
    }
}