package vn.vnsky.bcss.admin.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import vn.vnsky.bcss.admin.dto.ClientDTO;
import vn.vnsky.bcss.admin.entity.ClientEntity;

@Mapper(componentModel = "spring")
public interface ClientMapper extends BaseMapper<ClientDTO, ClientEntity> {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "contactName", source = "contactName")
    @Mapping(target = "contactEmail", source = "contactEmail")
    @Mapping(target = "contactPhone", source = "contactPhone")
    @Mapping(target = "contactPosition", source = "contactPosition")
    @Mapping(target = "permanentAddress", source = "permanentAddress")
    @Mapping(target = "permanentProvinceId", source = "permanentProvinceId")
    @Mapping(target = "permanentDistrictId", source = "permanentDistrictId")
    @Mapping(target = "permanentWardId", source = "permanentWardId")
    void patch(@MappingTarget ClientEntity clientEntity, ClientDTO clientDTO);

}
