package vn.vnsky.bcss.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.vnsky.bcss.admin.entity.ActionEntity;

import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<ActionEntity, String> {
    @Query(value = """
            Select a from ActionEntity a
            join ObjectActionEntity oa on a.id = oa.actionId
            join ObjectEntity o on o.id = oa.objectId
            where oa.objectId = :objectId and o.appId = :appId
            """)
    List<ActionEntity> findByJoinWithObject(String objectId, String appId);
}
