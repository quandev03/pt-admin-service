package vn.vnsky.bcss.admin.mapper;

import org.mapstruct.Mapper;
import vn.vnsky.bcss.admin.dto.ObjectActionDTO;
import vn.vnsky.bcss.admin.entity.ObjectActionEntity;

@Mapper(componentModel = "spring")
public interface ObjectActionMapper extends BaseMapper<ObjectActionDTO, ObjectActionEntity>{

}
