package vn.vnsky.bcss.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.vnsky.bcss.admin.dto.AuthorizedObjectActionProjectionDTO;
import vn.vnsky.bcss.admin.entity.ObjectActionEntity;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ObjectActionRepository extends JpaRepository<ObjectActionEntity, String>, ObjectActionRepositoryCustom {

    void deleteAllByObjectId(String objectId);

    Optional<ObjectActionEntity> findByObjectIdAndActionId(String objectId, String actionId);

    @Query(value = """
        Select distinct a.ID as ACTION_ID, a.NAME as ACTION_NAME from OBJECT_ACTION oa
        join ACTION a on oa.ACTION_ID = a.ID
        where oa.OBJECT_ID = :objectId
        and exists(select * from API_CATALOG_ACL acl where acl.ACL_ID = oa.id)
    """, nativeQuery = true)
    List<AuthorizedObjectActionProjectionDTO> getAuthorizedObjectActionByObjectId(String objectId);

    @Modifying
    @Query(value = """
        delete from OBJECT_ACTION oa
        where oa.OBJECT_ID = :objectId
        and oa.ACTION_ID not in :actionIds
    """, nativeQuery = true)
    void deleteByObjectIdAndActionIdNotIn(String objectId, Set<String> actionIds);
}