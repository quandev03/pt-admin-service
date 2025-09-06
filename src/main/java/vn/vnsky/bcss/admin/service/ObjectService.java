package vn.vnsky.bcss.admin.service;

import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;
import vn.vnsky.bcss.admin.dto.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ObjectService {

    ObjectResponseDTO create(CreateUpdateObjectDTO request);

    ObjectResponseDTO update(String id, CreateUpdateObjectDTO request);

    DeleteCountDTO delete(String id, boolean isPartner, Boolean isMobile);

    ObjectResponseDTO findById(String id, boolean isPartner, Boolean isMobile);

    List<ObjectResponseDTO> tree(boolean isPartner, Boolean isMobile, HttpHeaders headers);

    UserPolicyDTO retrieveUserPolicy(String userId, String appCode);

    ApiPolicyDTO retrieveApiPolicy(ApiPolicyDTO apiPolicy);

    boolean checkPolicy(Map<String, Set<String>> policyMap, List<PermissionDTO> permissions);

    @Nullable
    UserDTO refreshUserInfoCache(String userId);

    PolicyCheckDTO checkPolicy(PolicyCheckDTO policyCheck);

    void clearUserPolicyCache(Set<String> userIds, String appCode);

    PolicyCacheConfigDTO getCacheConfig();

    PolicyCacheConfigDTO updateCacheConfig(PolicyCacheConfigDTO policyCacheConfig);

    void registerSelfApiSet();

    void registerApiSet(ApiCatalogRegistrationDTO apiCatalogRegistration);

    void updateObjectActionName();

    Resource uploadAclPolicy(MultipartFile file);
}
