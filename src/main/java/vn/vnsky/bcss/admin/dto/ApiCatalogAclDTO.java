package vn.vnsky.bcss.admin.dto;

import lombok.*;
import vn.vnsky.bcss.admin.util.DbColumnMapper;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ApiCatalogAclDTO {
    @DbColumnMapper("CATALOG_ID")
    private String catalogId;

    @DbColumnMapper("ACL_ID")
    private String aclId;

    @DbColumnMapper("API_CATALOG_ID")
    private String apiCatalogId;

    @DbColumnMapper("OBJECT_ACTION_ID")
    private String objectActionId;
}