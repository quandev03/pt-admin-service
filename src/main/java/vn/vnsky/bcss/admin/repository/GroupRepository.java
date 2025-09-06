package vn.vnsky.bcss.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import vn.vnsky.bcss.admin.entity.ClientEntity;
import vn.vnsky.bcss.admin.entity.GroupEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data  repository for the Groups entity.
 */
@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, String> {

    @Query(value = """
            select distinct g from GroupEntity g
                               where g.client.id = :clientId and ( :term is null
                               or lower(g.code) like :term
                               or lower(g.name) like :term
                               ) and (:status is null or g.status = :status)
                               order by g.lastModifiedDate desc
            """
    )
    @EntityGraph(value = "Groups.graph")
    Page<GroupEntity> findByTerm(@Param("clientId") String clientId, @Param("term") String term, @Param("status") Integer status, Pageable pageable);

    @Override
    @EntityGraph(value = "Groups.graph")
    @NonNull
    Optional<GroupEntity> findById(@NonNull String id);

    @Query(value = "select count(1) from \"GROUP\" where id in :ids", nativeQuery = true)
    int findTotalById(@Param("ids") Set<String> id);

    @Query(value = """
            select case when count(gr.id) > 0 then true else false end
            from GroupEntity gr
            where gr.client.id = :clientId and gr.code = :code and (:id is null or gr.id <> :id)
            """)
    boolean existsByClientAndCode(@Param("clientId") String clientId, @Param("code") String code, @Param("id") String id);

    List<GroupEntity> findAllByIdIn(List<String> ids);

    List<GroupEntity> findAllByClient(ClientEntity clientEntity);

    @Query(value = """
                SELECT RU.USER_ID
                FROM ROLE_USER RU
                         INNER JOIN ROLE R ON RU.ROLE_ID = R.ID
                WHERE R.ID = :id
                UNION ALL
                SELECT GU.USER_ID
                FROM GROUP_ROLE RG
                         INNER JOIN GROUP_USER GU ON RG.GROUP_ID = GU.GROUP_ID
                         INNER JOIN ROLE R ON RG.ROLE_ID = R.ID
                WHERE R.ID = :id
            """, nativeQuery = true)
    Set<String> findAllAffectedUserIds(@Param("id") String id);

    Set<GroupEntity> findByIdInAndClient(Set<String> ids, ClientEntity clientEntity);

}
