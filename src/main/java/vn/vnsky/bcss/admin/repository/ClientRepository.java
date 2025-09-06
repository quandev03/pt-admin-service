package vn.vnsky.bcss.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vnsky.bcss.admin.entity.ClientEntity;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, String> {

    boolean existsByCodeOrId(String code, String id);

    @Query(value = """
                select c.* from CLIENT c
                 where c.CODE = :clientIdentity or c.ID = :clientIdentity
            """, nativeQuery = true)
    Optional<ClientEntity> findByIdOrAlias(@Param("clientIdentity") String clientIdentity);

    @Query(value = """
                select c.* from CLIENT c
                where (:term is null or c.CODE COLLATE BINARY_CI like :term or c.NAME COLLATE BINARY_CI like :term)
                                            and ((:clientType = 'VNSKY' and c.ID = '000000000000')
                or (:clientType = 'PARTNER' and c.ID <> '000000000000' and REGEXP_LIKE(c.ID, '\\d{12}$'))
                or (:clientType = 'PARTNER_AND_VNSKY' and REGEXP_LIKE(c.ID, '\\d{12}$'))
                or (:clientType = 'THIRD_PARTY' and REGEXP_LIKE(c.ID, '^3RD\\d{9}$')))
            """,
            countQuery = """
                select count(c.ID) from CLIENT c
                where (:term is null or c.CODE COLLATE BINARY_CI like :term or c.NAME COLLATE BINARY_CI like :term)
                                            and ((:clientType = 'VNSKY' and c.ID = '000000000000')
                or (:clientType = 'PARTNER' and c.ID <> '000000000000' and REGEXP_LIKE(c.ID, '\\d{12}$'))
                or (:clientType = 'PARTNER_AND_VNSKY' and REGEXP_LIKE(c.ID, '\\d{12}$'))
                or (:clientType = 'THIRD_PARTY' and REGEXP_LIKE(c.ID, '^3RD\\d{9}$')))
            """,
            nativeQuery = true)
    Page<ClientEntity> findByTerm(@Param("term") String term, @Param("clientType") String clientType, Pageable pageable);

    @Query(value = "select CLIENT_ORDINAL_SEQ.nextval from dual", nativeQuery = true)
    Long nextClientSeq();

}
