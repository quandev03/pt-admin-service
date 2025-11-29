package vn.vnsky.bcss.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vnsky.bcss.admin.entity.GroupUserEntity;
import vn.vnsky.bcss.admin.entity.GroupUserId;

public interface GroupUserRepository extends JpaRepository<GroupUserEntity, GroupUserId> {

    boolean existsByGroupIdAndUserId(String groupId, String userId);

    long deleteByGroupIdAndUserId(String groupId, String userId);
}

