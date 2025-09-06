package vn.vnsky.bcss.admin.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.vnsky.bcss.admin.dto.ObjectActionDTO;
import vn.vnsky.bcss.admin.repository.ObjectActionRepositoryCustom;
import vn.vnsky.bcss.admin.util.DbMapper;

import java.util.List;

@Repository
public class ObjectActionRepositoryImpl implements ObjectActionRepositoryCustom {

    private static final String ACL_SQL = """
            WITH ACL AS (
                SELECT AA.* FROM OBJECT_ACTION AA
                INNER JOIN API_CATALOG_ACL ACA ON AA.ID = ACA.ACL_ID
                WHERE ACA.CATALOG_ID = :apiCatalogId
            )
            SELECT ACL.*, O.CODE AS OBJECT_CODE, o.NAME AS OBJECT_NAME, A.CODE AS ACTION_CODE, A.NAME AS ACTION_NAME
            FROM ACL
            INNER JOIN OBJECT O ON ACL.OBJECT_ID = O.ID
            INNER JOIN ACTION A ON ACL.ACTION_ID = A.ID
            """;

    @PersistenceContext
    private EntityManager entityManager;

    private final DbMapper dbMapper;

    @Autowired
    public ObjectActionRepositoryImpl(DbMapper dbMapper) {
        this.dbMapper = dbMapper;
    }

    @Override
    public List<ObjectActionDTO> findByApiCatalogId(String apiCatalogId) {
        Query query = this.entityManager.createNativeQuery(ACL_SQL, Tuple.class);
        query.setParameter("apiCatalogId", apiCatalogId);
        List<Tuple> resultSet = query.getResultList();
        return this.dbMapper.castSqlResult(resultSet, ObjectActionDTO.class);
    }

}
