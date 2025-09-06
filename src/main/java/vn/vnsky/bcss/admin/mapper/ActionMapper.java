package vn.vnsky.bcss.admin.mapper;

import org.mapstruct.Mapper;
import vn.vnsky.bcss.admin.dto.ActionDTO;
import vn.vnsky.bcss.admin.entity.ActionEntity;

@Mapper(componentModel = "spring")
public interface ActionMapper extends BaseMapper<ActionDTO, ActionEntity> {

}