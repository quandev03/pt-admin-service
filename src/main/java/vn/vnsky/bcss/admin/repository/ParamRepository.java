package vn.vnsky.bcss.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vnsky.bcss.admin.dto.ParamDTO;
import vn.vnsky.bcss.admin.entity.ParamEntity;

import java.util.List;

public interface ParamRepository extends JpaRepository<ParamEntity, String> {

    @Query("Select p.value from ParamEntity p WHERE p.type='RESET_PASSWORD' and p.code='PASSWORD_ACTIVE' and p.status= 1 and p.appCode='WEB'")
    String getTimeChangePass();

    @Query("Select p.value from ParamEntity p WHERE p.type='FORGOT_PASSWORD' and p.code='URL' and p.status= 1 and p.appCode= :appCode ")
    String getDomainForgotPasswordUrl(@Param("appCode") String appCode);

    @Query("Select p.value from ParamEntity p WHERE p.type='COMPANY_NAME' and p.code='MAIL_COMPANY_NAME' and p.status= 1 and p.appCode='WEB'")
    String getCompanyName();

    @Query("SELECT new vn.vnsky.bcss.admin.dto.ParamDTO(p.description, p.value) FROM ParamEntity p WHERE p.code = :code AND p.status = :status AND p.translations = :translations ")
    List<ParamDTO> getParamByParamCode(@Param("code") String code, @Param("status") int status, @Param("translations") String translations);

    @Query("select p FROM ParamEntity p WHERE p.code NOT IN :codes AND p.status = :status AND p.translations = :translations ")
    List<ParamEntity> getParamEntityByCodeNotIn(@Param("codes") List<String> codes, @Param("status") int status, @Param("translations") String translations);

    @Query("Select p from ParamEntity p WHERE p.code= :code and p.status= 1")
    List<ParamEntity> findByParamCode(@Param("code") String code);
}
