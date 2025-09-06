package vn.vnsky.bcss.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.vnsky.bcss.admin.entity.FcmTokenEntity;

import java.util.List;

@Repository
public interface FcmUserTokenRepository extends JpaRepository<FcmTokenEntity, String> {

    List<FcmTokenEntity> findByClientIdAndUserId(String clientId, String userId);

}
