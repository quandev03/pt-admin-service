package vn.vnsky.bcss.admin.repository;

import vn.vnsky.bcss.admin.dto.MenuItemDTO;
import vn.vnsky.bcss.admin.dto.ObjectActionDTO;
import vn.vnsky.bcss.admin.dto.ObjectResponseDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author thanhvt
 * @created 12/04/2023 - 10:19 SA
 * @project str-auth
 * @since 1.0
 **/
public interface ObjectRepositoryCustom {

    List<MenuItemDTO> getMenuItemHierarchy(String userId, boolean isOwner, String appCode);

    List<MenuItemDTO> getMenuItemFlat(String userId, boolean isOwner, String appCode);

    List<ObjectResponseDTO> getObjectHierarchy(String appCode);

    List<ObjectActionDTO> findAllObjectActionByObjectCodes(String appId, Set<String> objectCodes);

    Map<String, Set<String>> getUserPermissionList(String userId, String appCode);

}
