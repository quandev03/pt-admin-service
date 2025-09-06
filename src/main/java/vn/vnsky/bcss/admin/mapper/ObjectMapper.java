package vn.vnsky.bcss.admin.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import vn.vnsky.bcss.admin.dto.CreateUpdateObjectDTO;
import vn.vnsky.bcss.admin.dto.ObjectResponseDTO;
import vn.vnsky.bcss.admin.entity.ObjectEntity;

@Mapper(componentModel = "spring")
public interface ObjectMapper extends BaseMapper<ObjectResponseDTO, ObjectEntity> {

    ObjectEntity createDtoToEntity(CreateUpdateObjectDTO request);

    void updateRequestDtoToEntity(CreateUpdateObjectDTO request, @MappingTarget ObjectEntity object);

}