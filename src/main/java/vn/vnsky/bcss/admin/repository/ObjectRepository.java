package vn.vnsky.bcss.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vnsky.bcss.admin.entity.ObjectEntity;

import java.util.List;
import java.util.Optional;


public interface ObjectRepository extends JpaRepository<ObjectEntity, String>, ObjectRepositoryCustom {

    Optional<ObjectEntity> findByIdAndAppId(String id, String appId);

    @Query(value = "SELECT COUNT(*) > 0 FROM \"USER\" U WHERE U.ID = :userId and U.STATUS = :activeStatus", nativeQuery = true)
    boolean isUserValid(@Param("userId") String userId, @Param("activeStatus") Integer activeStatus);

    boolean existsByAppIdAndCode(String appId, String code);

    @Query(value = "select CONCAT(CONCAT(roa.object_id, :idSeparator), roa.action_id) from role_object_action roa where role_id = :roleId", nativeQuery = true)
    List<String> getRoleObjectKeys(@Param("roleId") String roleId, @Param("idSeparator") String idSeparator);

    List<ObjectEntity> findByParentId(String parentId);

    @Modifying
    @Query(value = """
            insert into role_object_action (role_id, object_id, action_id) \s
             values (:roleId, :objectId, :actionId)\s
            """, nativeQuery = true)
    void insertRoleObjectAction(@Param("roleId") String roleId, @Param("objectId") String objectId, @Param("actionId") String actionId);

    @Modifying
    @Query(value = "DELETE FROM role_object_action WHERE role_id =:roleId", nativeQuery = true)
    int deleteRoleObjectAction(@Param("roleId") String roleId);

    @Modifying(flushAutomatically = true)
    @Query(value = "INSERT INTO CURRENT_OBJECT_ACTION(OBJECT_ID, ACTION_ID) VALUES (:objectId, :actionId)", nativeQuery = true)
    void insertCurrentObjectAction(@Param("objectId") String objectId, @Param("actionId") String actionId);


    @Query(value = "SELECT COUNT(*) FROM CURRENT_OBJECT_ACTION", nativeQuery = true)
    int countCurrentObjectAction();



}
