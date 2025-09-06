package vn.vnsky.bcss.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "API_CATALOG_ACL")
@FieldNameConstants
@IdClass(ApiGroupAclEntity.ApiGroupAclKey.class)
public class ApiGroupAclEntity {
    @Id
    @Column(name = "CATALOG_ID")
    private String catalogId;

    @Id
    @Column(name = "ACL_ID")
    private String aclID;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApiGroupAclKey implements Serializable {
        private String catalogId;
        private String aclID;
    }
}