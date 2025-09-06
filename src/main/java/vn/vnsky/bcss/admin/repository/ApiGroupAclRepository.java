package vn.vnsky.bcss.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.vnsky.bcss.admin.entity.ApiGroupAclEntity;

public interface ApiGroupAclRepository extends JpaRepository<ApiGroupAclEntity, ApiGroupAclEntity.ApiGroupAclKey> {
    @Modifying
    @Query(value = """
        delete from API_GROUP_ACL acl
        where acl.ACL_ID in (select id from OBJECT_ACTION where OBJECT_ID = :objectId)
    """, nativeQuery = true)
    void deleteByObjectId(String objectId);
}