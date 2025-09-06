package vn.vnsky.bcss.admin.repository;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.vnsky.bcss.admin.dto.UploadAclPolicyRequestDTO;
import vn.vnsky.bcss.admin.entity.ApiCatalogAclEntity;

public interface ApiCatalogAclRepository extends JpaRepository<ApiCatalogAclEntity, ApiCatalogAclEntity.ApiCatalogAclKey> {
    boolean existsByCatalogIdAndAclID(String catalogId, String aclID);

    @Query(value = """
        select ac.id as API_CATALOG_ID,
               oa.id AS OBJECT_ACTION_ID,
               aac.ACL_ID,
               aac.CATALOG_ID
        from API_CATALOG ac
        left join OBJECT o on o.CODE = :#{#request.objectCode}
        left join OBJECT_ACTION oa on o.id = oa.OBJECT_ID
        left join action a on oa.ACTION_ID = a.id
        left join API_CATALOG_ACL aac on ac.id = aac.CATALOG_ID and oa.id = aac.ACL_ID
        where ac.URI_PATTERN = :#{#request.urlPattern} and ac.METHOD = :#{#request.method} and ac.SERVICE_CODE = :#{#request.serviceCode}
        and a.code = :#{#request.actionCode}
        and oa.NAME like (:#{#request.clientId} || '%')
    """, nativeQuery = true)
    Tuple findForUploadAcl(UploadAclPolicyRequestDTO request);

    @Query(value = """
        select case
                    when count(*) > 0 then 'true'
                    else 'false'
                end
        from OBJECT_ACTION oa join API_CATALOG_ACL acl on oa.id = acl.ACL_ID where oa.OBJECT_ID = :objectId
    """, nativeQuery = true)
    boolean checkObjectIsAuthorized(String objectId);
}