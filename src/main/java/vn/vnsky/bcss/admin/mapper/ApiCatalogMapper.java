package vn.vnsky.bcss.admin.mapper;

import org.mapstruct.Mapper;
import vn.vnsky.bcss.admin.dto.ApiCatalogDTO;
import vn.vnsky.bcss.admin.entity.ApiCatalogEntity;

@Mapper(componentModel = "spring")
public interface ApiCatalogMapper extends BaseMapper<ApiCatalogDTO, ApiCatalogEntity> {
}
