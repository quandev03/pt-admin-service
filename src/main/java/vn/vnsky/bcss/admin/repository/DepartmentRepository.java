package vn.vnsky.bcss.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.vnsky.bcss.admin.entity.DepartmentEntity;

import java.util.Set;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

    @Query(value = "select count(1) from \"DEPARTMENT\" where id in :ids", nativeQuery = true)
    int findTotalById(@Param("ids") Set<Long> ids);

    DepartmentEntity findByCode(String code);
}
