package vn.vnsky.bcss.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.RoleDTO;

import java.util.List;

public interface RoleService {

    Page<RoleDTO> search(boolean isPartner, String term, Integer status, Pageable pageable);

    List<RoleDTO> all(boolean isPartner);

    RoleDTO create(boolean isPartner, Boolean isMobile, RoleDTO roleDTO);

    RoleDTO update(boolean isPartner, Boolean isMobile, String id, RoleDTO roleDTO);

    RoleDTO detail(boolean isPartner, String id);

    DeleteCountDTO delete(boolean isPartner, List<String> ids);

}
