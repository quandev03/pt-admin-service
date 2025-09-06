package vn.vnsky.bcss.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.vnsky.bcss.admin.dto.DepartmentDTO;
import vn.vnsky.bcss.admin.mapper.DepartmentMapper;
import vn.vnsky.bcss.admin.repository.DepartmentRepository;
import vn.vnsky.bcss.admin.service.DepartmentService;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    private final DepartmentMapper departmentMapper;

    @Autowired
    public DepartmentServiceImpl(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    @Override
    public List<DepartmentDTO> all() {
        return this.departmentRepository.findAll()
                .stream()
                .map(this.departmentMapper::toDto)
                .toList();
    }

    @Override
    public DepartmentDTO findByDepartmentCode(String code) {
        return this.departmentMapper.toDto(this.departmentRepository.findByCode(code));
    }

}
