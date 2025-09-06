package vn.vnsky.bcss.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.vnsky.bcss.admin.entity.RoleEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {

    @Query(value = "select count(1) from \"ROLE\" where id in :ids", nativeQuery = true)
    int findTotalById(@Param("ids") Set<String> roleIds);

    Optional<RoleEntity> findByIdAndAppId(String id, String appId);

    Optional<RoleEntity> findByIdAndAppIdIn(String id, List<String> appId);

    Set<RoleEntity> findByIdInAndAppId(Set<String> ids, String appId);

    Set<RoleEntity> findByIdInAndAppIdIn(Set<String> ids, List<String> appIds);

    @Query("""
            select new vn.vnsky.bcss.admin.entity.RoleEntity(r.id, r.name, r.code, r.status ) from RoleEntity r
                               where r.appId = :appId
                               order by r.status DESC, r.lastModifiedDate desc
            """)
    List<RoleEntity> findAllByAppId(@Param("appId") String appId);

    @Query("""
            select new vn.vnsky.bcss.admin.entity.RoleEntity(r.id, r.name, r.code, r.status ) from RoleEntity r
                               where r.appId in :appIds
                               order by r.status DESC, r.lastModifiedDate desc
            """)
    List<RoleEntity> findAllByAppIdIn(List<String> appIds);

    @Query("""
                SELECT CASE WHEN count(r.id) > 0 THEN 'true' ELSE 'false' END
                       FROM RoleEntity r
                       WHERE r.appId = :appId and (LOWER(r.code) = LOWER(:roleCode))
            """
    )
    boolean existsByRoleCodeCreate(@Param("appId") String appId, @Param("roleCode") String roleCode);

    @Query("""
            SELECT CASE WHEN count(r.id) > 0 THEN 'true' ELSE 'false' END
                    FROM RoleEntity r
                    WHERE r.appId = :appId and lower(r.code) = lower(:roleCode)
                    AND (r.id != :roleId)
            """
    )
    boolean existsByRoleCodeUpdate(@Param("roleId") String roleId, @Param("appId") String appId, @Param("roleCode") String roleCode);

    @Modifying
    @Query(value = "delete from role_user where role_id = :roleId", nativeQuery = true)
    void deleteUserRole(@Param("roleId") String roleId);

    @Modifying
    @Query(value = "delete from group_role where role_id = :roleId", nativeQuery = true)
    void deleteGroupRole(@Param("roleId") String roleId);

    @Query("""
            select distinct r from RoleEntity r
                               where ((:term is null
                               or lower(r.code) like :term
                               or lower(r.name) like :term
                               ) and r.appId = :appId and (:status is null or r.status = :status))
                               order by r.lastModifiedDate desc
            """
    )
    Page<RoleEntity> findByTerm(@Param("term") String term, @Param("appId") String appId, Integer status, Pageable pageable);

    @Query("""
            select distinct r from RoleEntity r
                               where ((:term is null
                               or lower(r.code) like :term
                               or lower(r.name) like :term
                               ) and r.appId in :appIds and (:status is null or r.status = :status))
                               order by r.lastModifiedDate desc
            """
    )
    Page<RoleEntity> findByTermIn(@Param("term") String term, List<String> appIds, Integer status, Pageable pageable);

    @Query(value = """
                SELECT RU.USER_ID
                FROM ROLE_USER RU
                         INNER JOIN ROLE R ON RU.ROLE_ID = R.ID
                WHERE R.ID = :id
            """, nativeQuery = true)
    Set<String> findAllAffectedUserIds(@Param("id") String id);

}
