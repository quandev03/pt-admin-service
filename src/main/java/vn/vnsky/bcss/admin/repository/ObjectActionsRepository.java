package vn.vnsky.bcss.admin.repository;

import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vnsky.bcss.admin.entity.ObjectActionEntity;

import java.util.List;

public interface ObjectActionsRepository extends JpaRepository<ObjectActionEntity, String> {
    @Query(value = """
                WITH ACL AS (
                        SELECT AA.* FROM OBJECT_ACTION AA
                        INNER JOIN API_CATALOG_ACL ACA ON AA.ID = ACA.ACL_ID
                        WHERE ACA.CATALOG_ID = :apiCatalogId
                    )
                    SELECT ACL.*, O.CODE AS OBJECT_CODE, o.NAME AS OBJECT_NAME, A.CODE AS ACTION_CODE, A.NAME AS ACTION_NAME
                    FROM ACL
                    INNER JOIN OBJECT O ON ACL.OBJECT_ID = O.ID
                    INNER JOIN ACTION A ON ACL.ACTION_ID = A.ID
            """, nativeQuery = true)
    List<Tuple> getPermissions(@Param("apiCatalogId") String apiCatalogId);
}
