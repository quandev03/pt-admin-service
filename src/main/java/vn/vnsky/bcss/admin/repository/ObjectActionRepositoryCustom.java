package vn.vnsky.bcss.admin.repository;

import vn.vnsky.bcss.admin.dto.ObjectActionDTO;

import java.util.List;

public interface ObjectActionRepositoryCustom {

    List<ObjectActionDTO> findByApiCatalogId(String apiCatalogId);

}
