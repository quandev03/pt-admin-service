package vn.vnsky.bcss.admin.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vnsky.bcss.admin.constant.ErrorMessageConstant;
import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.GroupUserRequestDTO;
import vn.vnsky.bcss.admin.dto.GroupUserResponseDTO;
import vn.vnsky.bcss.admin.entity.GroupUserEntity;
import vn.vnsky.bcss.admin.error.BusinessException;
import vn.vnsky.bcss.admin.repository.GroupUserRepository;
import vn.vnsky.bcss.admin.service.GroupUserService;

@Service
@Transactional
public class GroupUserServiceImpl implements GroupUserService {

    private final GroupUserRepository groupUserRepository;

    public GroupUserServiceImpl(GroupUserRepository groupUserRepository) {
        this.groupUserRepository = groupUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(String groupId, String userId) {
        return groupUserRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public GroupUserResponseDTO create(GroupUserRequestDTO request) {
        String groupId = request.getGroupId();
        String userId = request.getUserId();
        if (groupUserRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new BusinessException(ErrorMessageConstant.GROUP_USER_ALREADY_EXISTS);
        }
        GroupUserEntity entity = new GroupUserEntity();
        entity.setGroupId(groupId);
        entity.setUserId(userId);
        GroupUserEntity saved = groupUserRepository.save(entity);
        return GroupUserResponseDTO.builder()
                .groupId(saved.getGroupId())
                .userId(saved.getUserId())
                .build();
    }

    @Override
    public DeleteCountDTO delete(GroupUserRequestDTO request) {
        long deleted = groupUserRepository.deleteByGroupIdAndUserId(request.getGroupId(), request.getUserId());
        return new DeleteCountDTO((int) deleted);
    }
}

