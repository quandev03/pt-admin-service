package vn.vnsky.bcss.admin.mapper;

import org.mapstruct.*;
import vn.vnsky.bcss.admin.dto.RoleDTO;
import vn.vnsky.bcss.admin.entity.RoleEntity;

import java.util.List;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.WARN, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface RoleMapper extends BaseMapper<RoleDTO, RoleEntity> {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "status", source = "status")
    @Named("ignore")
    RoleDTO toDtoIgnore(RoleEntity entity);

    @IterableMapping(qualifiedByName = "ignore")
    List<RoleDTO> toDtoIgnore(List<RoleEntity> entity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "status", source = "status")
    void patch(@MappingTarget RoleEntity roleEntity, RoleDTO roleDTO);
}
