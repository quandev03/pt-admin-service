package vn.vnsky.bcss.admin.service;

import vn.vnsky.bcss.admin.dto.DepartmentDTO;

import java.util.List;

public interface DepartmentService {

    List<DepartmentDTO> all();

    DepartmentDTO findByDepartmentCode(String code);
}
