package vn.vnsky.bcss.admin.service;

import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.GroupUserRequestDTO;
import vn.vnsky.bcss.admin.dto.GroupUserResponseDTO;

public interface GroupUserService {

    boolean exists(String groupId, String userId);

    GroupUserResponseDTO create(GroupUserRequestDTO request);

    DeleteCountDTO delete(GroupUserRequestDTO request);
}

