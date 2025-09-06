package vn.vnsky.bcss.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.vnsky.bcss.admin.entity.ApiCatalogEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiCatalogRepository extends JpaRepository<ApiCatalogEntity, String> {

    @Modifying
    @Query(value = """
        UPDATE API_CATALOG SET STATUS = -1 WHERE SERVICE_CODE = :serviceCode AND STATUS <> 1
    """, nativeQuery = true)
    void markForDeletion(@Param("serviceCode") String serviceCode);

    @Modifying
    @Query(value = """
                MERGE INTO API_CATALOG
                USING DUAL
                ON (SERVICE_CODE = :#{#apiCatalogEntity.serviceCode} AND URI_PATTERN = :#{#apiCatalogEntity.uriPattern} AND METHOD = :#{#apiCatalogEntity.method})
                    WHEN MATCHED THEN
                        UPDATE SET NAME = :#{#apiCatalogEntity.name}, STATUS = :#{#apiCatalogEntity.status}
                        WHERE STATUS = -1
                    WHEN NOT MATCHED THEN
                        INSERT (ID, NAME, SERVICE_CODE, URI_PATTERN, METHOD, STATUS)
                        VALUES (:#{#apiCatalogEntity.id}, :#{#apiCatalogEntity.name}, :#{#apiCatalogEntity.serviceCode}, :#{#apiCatalogEntity.uriPattern}, :#{#apiCatalogEntity.method}, :#{#apiCatalogEntity.status})
            """, nativeQuery = true)
    void upsert(@Param("apiCatalogEntity") ApiCatalogEntity apiCatalogEntity);

    Optional<ApiCatalogEntity> findByServiceCodeAndUriPatternAndMethod(String serviceCode, String uriPattern, String method);

     @Modifying
    @Query(value = """
        Update API_CATALOG set status = 1 where ID in :ids
    """, nativeQuery = true)
    void updateStatusToActiveByIds(List<String> ids);
}
