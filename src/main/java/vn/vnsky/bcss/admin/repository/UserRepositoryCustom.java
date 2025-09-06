package vn.vnsky.bcss.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.vnsky.bcss.admin.dto.UserDTO;

import java.util.List;

public interface UserRepositoryCustom {

    Page<UserDTO> findByTerm(String term, List<Integer> status, String clientId, Pageable pageable);

    List<UserDTO> findAllByClientIdAndPermissions(String clientId, List<String> permissions);

    List<UserDTO> findAllByClientIdAndDepartmentCode(String clientId, String departmentCode);

    List<UserDTO> findAllByClientType(Boolean isPartner, String term);
}
