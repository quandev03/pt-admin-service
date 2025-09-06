package vn.vnsky.bcss.admin.mapper;

import org.mapstruct.Mapper;
import vn.vnsky.bcss.admin.dto.DepartmentDTO;
import vn.vnsky.bcss.admin.entity.DepartmentEntity;

@Mapper(componentModel = "spring")
public interface DepartmentMapper extends BaseMapper<DepartmentDTO, DepartmentEntity> {

}
