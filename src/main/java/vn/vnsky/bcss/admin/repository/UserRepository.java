package vn.vnsky.bcss.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vnsky.bcss.admin.entity.ClientEntity;
import vn.vnsky.bcss.admin.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String>, UserRepositoryCustom {

    @EntityGraph(value = "Users.graph")
    Optional<UserEntity> findByClientIdAndUsername(String clientId, String username);

    @EntityGraph(value = "Users.graph")
    Optional<UserEntity> findByClientIdAndEmail(String clientId, String email);

    List<UserEntity> findAllByClientOrderByLastModifiedDateDescCreatedDateDesc(ClientEntity clientEntity);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            update "USER" set PASSWORD = :password,
            PASSWORD_EXPIRE_TIME = :passwordExpireTime,
            STATUS = CASE WHEN :status is null THEN STATUS ELSE :status END,
            LOGIN_FAILED_COUNT = CASE WHEN :loginFailedCount is null THEN LOGIN_FAILED_COUNT ELSE :loginFailedCount END
            WHERE ID = :userId
            """, nativeQuery = true)
    void updatePassword(@Param("password") String password,
                        @Param("passwordExpireTime") LocalDateTime passwordExpireTime,
                        @Param("status") Integer status,
                        @Param("loginFailedCount") Integer loginFailedCount,
                        @Param("userId") String userId);

    @Modifying
    @Query(value = "update FCM_USER_TOKEN set CLIENT_ID = null, USER_ID = null where CLIENT_ID = :clientId and USER_ID = :userId", nativeQuery = true)
    void releaseFcmTokensByClientIdAndUserId(@Param("clientId") String clientId, @Param("userId") String userId);

    @Query(value = """
        Select * from "USER" u
        where u.client_id <> :vnskyClientId
        and (:status is null or u.STATUS = :status)
        and (:term is null or UPPER(u.FULLNAME) LIKE :term or UPPER(u.USERNAME) like :term)
        """, nativeQuery = true)
    Page<UserEntity> findPartnerUsers(String vnskyClientId, String term, Integer status, Pageable pageable);

    @Query(value = """
            select distinct ru.USER_ID from OBJECT o
            join ROLE_OBJECT_ACTION roc on o.id = roc.OBJECT_ID
            left join ACTION a on roc.ACTION_ID = a.ID
            join ROLE_USER ru on roc.ROLE_ID = ru.ROLE_ID
            where o.code in :objectCodes
        """, nativeQuery = true)
    List<String> findByObjectCode(List<String> objectCodes);
}
