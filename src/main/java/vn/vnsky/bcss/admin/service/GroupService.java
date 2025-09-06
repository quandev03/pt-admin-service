package vn.vnsky.bcss.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.GroupDTO;

import java.util.List;

/**
 * @author thomas_luu
 * @created 31/03/2023 - 1:19 PM
 * @project str-auth
 */
public interface GroupService {

    Page<GroupDTO> search(String clientIdentity, String term, Integer status, Pageable pageable);

    List<GroupDTO> all(String clientIdentity);

    GroupDTO detail(String clientIdentity, String id);

    GroupDTO create(String clientIdentity, GroupDTO groupDTO);

    GroupDTO update(String clientIdentity, String id, GroupDTO groupDTO);

    DeleteCountDTO delete(String clientIdentity, List<String> ids);

}
