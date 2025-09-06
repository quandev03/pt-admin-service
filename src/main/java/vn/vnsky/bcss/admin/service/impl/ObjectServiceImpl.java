package vn.vnsky.bcss.admin.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.vnsky.common.exception.domain.BaseException;
import com.vnsky.common.exception.domain.ErrorKey;
import com.vnsky.excel.dto.ExcelData;
import com.vnsky.excel.service.XlsxOperations;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.constant.CacheKey;
import vn.vnsky.bcss.admin.constant.ErrorMessageConstant;
import vn.vnsky.bcss.admin.dto.*;
import vn.vnsky.bcss.admin.entity.*;
import vn.vnsky.bcss.admin.error.BusinessException;
import vn.vnsky.bcss.admin.error.FieldsValidationException;
import vn.vnsky.bcss.admin.mapper.ApiCatalogMapper;
import vn.vnsky.bcss.admin.mapper.ObjectMapper;
import vn.vnsky.bcss.admin.mapper.UserMapper;
import vn.vnsky.bcss.admin.repository.*;
import vn.vnsky.bcss.admin.service.DeviceService;
import vn.vnsky.bcss.admin.service.ObjectService;
import vn.vnsky.bcss.admin.util.DataUtil;
import vn.vnsky.bcss.admin.util.DbMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ObjectServiceImpl implements ObjectService {

    public static final String WILDCARD_ACTION = "*";
    private final ApiCatalogAclRepository apiCatalogAclRepository;

    @Value("${spring.application.name}")
    private String serviceCode;

    private boolean userInfoCacheOn = true;

    private boolean apiAclCacheOn = true;

    private boolean userPolicyCacheOn = true;

    private final ApplicationProperties applicationProperties;

    private final RedisOperations<Object, Object> redisOperations;

    private final ActionRepository actionRepository;

    private final ObjectRepository objectRepository;

    private final ObjectActionRepository objectActionRepository;

    private final UserRepository userRepository;

    private final ApiCatalogRepository apiCatalogRepository;

    private final ApiCatalogMapper apiCatalogMapper;

    private final ObjectMapper objectMapper;

    private final UserMapper userMapper;

    private final ApplicationContext applicationContext;

    private final Cache userInfoCache;

    private final Cache apiPolicyCache;

    private final Cache userPolicyCache;

    private final DeviceService deviceService;

    private final XlsxOperations xlsxOperations;

    private final DbMapper dbMapper;

    private final ApiGroupAclRepository apiGroupAclRepository;

    private static final String API_CATALOG_NOT_FOUND = "Api catalog không tồn tại";

    private static final String OBJECT_ACTION_NOT_FOUND = "Object action không tồn tại";

    private static final String EXISTED_API_CATALOG_ACL = "Đã tồn tại phân quyền";

    private static final String CAN_NOT_DELETE_AUTHORIZED_ACTION = "Không thể xóa Action '%s' khi đã được phân quyền";

    private static final String CAN_NOT_DELETE_AUTHORIZED_OBJECT = "Không thể xóa đối tượng đã được phân quyền";

    @Autowired
    public ObjectServiceImpl(ApplicationProperties applicationProperties,
                             CacheManager cacheManager, @Qualifier("redisTemplate") RedisOperations<Object, Object> redisOperations,
                             ActionRepository actionRepository, ObjectRepository objectRepository,
                             ObjectActionRepository objectActionRepository, UserRepository userRepository,
                             ApiCatalogRepository apiCatalogRepository, ApiCatalogMapper apiCatalogMapper,
                             ObjectMapper objectMapper, UserMapper userMapper,
                             ApplicationContext applicationContext,
                             DeviceService deviceService,
                             XlsxOperations xlsxOperations,
                             DbMapper dbMapper, ApiCatalogAclRepository apiCatalogAclRepository,
                             ApiGroupAclRepository apiGroupAclRepository) {
        this.applicationProperties = applicationProperties;
        this.redisOperations = redisOperations;
        this.actionRepository = actionRepository;
        this.objectRepository = objectRepository;
        this.objectActionRepository = objectActionRepository;
        this.userRepository = userRepository;
        this.apiCatalogRepository = apiCatalogRepository;
        this.apiCatalogMapper = apiCatalogMapper;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
        this.applicationContext = applicationContext;
        this.userInfoCache = Objects.requireNonNull(cacheManager.getCache(CacheKey.USER_INFO_PREFIX));
        this.apiPolicyCache = Objects.requireNonNull(cacheManager.getCache(CacheKey.API_ACL_PREFIX));
        this.userPolicyCache = cacheManager.getCache(CacheKey.USER_POLICY_PREFIX);
        this.xlsxOperations = xlsxOperations;
        this.dbMapper = dbMapper;
        this.apiCatalogAclRepository = apiCatalogAclRepository;
        this.deviceService = deviceService;
        this.apiGroupAclRepository = apiGroupAclRepository;
    }

    @Transactional
    @Override
    public ObjectResponseDTO create(CreateUpdateObjectDTO request) {
        log.debug("Create object");
        String appId = deviceService.getWebClientInfo(request.getIsPartner(), request.getIsMobile()).getId();

        HashMap<String, String> errorFields = new HashMap<>();

        if (StringUtils.hasText(request.getParentId()) &&
            !objectRepository.existsById(request.getParentId())) {
            errorFields.put("parentId", ErrorMessageConstant.OBJECT_ID_NOT_FOUND);
            throw new FieldsValidationException(errorFields);
        }
        if (this.checkDuplicateByAppIdAndCode(appId, request.getCode(), errorFields)) {
            throw new FieldsValidationException(errorFields);
        }

        ObjectEntity objectEntity = objectMapper.createDtoToEntity(request);
        objectEntity.setAppId(appId);
        objectEntity.setStatus(AuthConstants.ModelStatus.ACTIVE);
        objectEntity = objectRepository.save(objectEntity);
        ObjectResponseDTO objectResponse = ObjectResponseDTO.builder()
                .key(objectEntity.getId())
                .uri(objectEntity.getUrl())
                .title(objectEntity.getName())
                .code(objectEntity.getCode())
                .children(new ArrayList<>())
                .parentId(objectEntity.getParentId())
                .ordinal(objectEntity.getOrdinal())
                .build();

        Map<String, ActionEntity> actionIdMap = this.actionRepository.findAll()
                .stream()
                .distinct()
                .collect(Collectors.toMap(ActionEntity::getId, e -> e));

        if (!ObjectUtils.isEmpty(request.getActionIds())) {
            saveListObjectAction(objectResponse, request.getActionIds(), actionIdMap, new HashSet<>());
        }
        return objectResponse;
    }

    public void saveListObjectAction(ObjectResponseDTO objectResponse, List<String> actionCodes, Map<String, ActionEntity> actionIdMap, Set<String> authorizedActions) {
        Set<String> addedActionIds = new HashSet<>();
        Map<String, String> errorFields = new HashMap<>();
        for (int actionIdIndex = 0; actionIdIndex < actionCodes.size(); actionIdIndex++) {
            String actionCode = actionCodes.get(actionIdIndex);

            //Nếu action đã được phân quyền thì bỏ qua nó
            if(authorizedActions.contains(actionCode)){
                continue;
            }

            if (!addedActionIds.add(actionCode)) {
                errorFields.put(String.format("actionIds[%s]", actionIdIndex),
                        ErrorMessageConstant.OBJECT_ACTION_ID_DUPLICATE);
                throw new FieldsValidationException(errorFields);
            }
            ActionEntity action = actionIdMap.get(actionCode);
            if (action == null) {
                errorFields.put(String.format("actionIds[%s]", actionIdIndex),
                        ErrorMessageConstant.ACTION_ID_NOT_FOUND);
                throw new FieldsValidationException(errorFields);
            } else {
                ObjectActionEntity objectActionEntity = this.objectActionRepository
                        .findByObjectIdAndActionId(objectResponse.getKey(), action.getId())
                        .orElse(ObjectActionEntity.builder()
                                .objectId(objectResponse.getKey())
                                .actionId(action.getId())
                                .status(AuthConstants.ModelStatus.ACTIVE)
                                .build());
                objectActionEntity.setName(objectResponse.getCode() + AuthConstants.CommonSymbol.FORWARD_SLASH + action.getCode());
                this.objectActionRepository.saveAndFlush(objectActionEntity);

                ObjectResponseDTO objectActionResponse = ObjectResponseDTO.builder()
                        .key(objectResponse.getKey() + "-" + action.getId())
                        .title(action.getName())
                        .build();
                objectResponse.getChildren().add(objectActionResponse);
            }
        }
    }

    private boolean checkDuplicateByAppIdAndCode(String appId, String code, HashMap<String, String> fieldErrors) {
        if (objectRepository.existsByAppIdAndCode(appId, code)) {
            fieldErrors.put("code", ErrorMessageConstant.OBJECT_APP_ID_AND_CODE_EXISTED);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public ObjectResponseDTO update(String id, CreateUpdateObjectDTO request) {
        log.debug("Update object");
        String appId = deviceService.getWebClientInfo(request.getIsPartner(), request.getIsMobile()).getId();

        ObjectEntity object = objectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.OBJECT_NOT_FOUND));

        //Kiểm tra xem có action nào của object được phân quyền api chưa
        Set<AuthorizedObjectActionProjectionDTO> authorizedActions = checkDeleteActionOfObject(id, request.getActionIds());
        Set<String> authorizedActionIds = authorizedActions.stream().map(AuthorizedObjectActionProjectionDTO::getActionId).collect(Collectors.toSet());

        if (!Objects.equals(object.getAppId(), appId) ||
            !Objects.equals(object.getCode(), request.getCode())) {
            HashMap<String, String> fieldErrors = new HashMap<>();
            if (this.checkDuplicateByAppIdAndCode(appId, request.getCode(), fieldErrors))
                throw new FieldsValidationException(fieldErrors);
        }

        Map<String, ActionEntity> actionIdMap = this.actionRepository.findAll()
                .stream()
                .distinct()
                .collect(Collectors.toMap(ActionEntity::getId, e -> e));

        objectMapper.updateRequestDtoToEntity(request, object);
        object = this.objectRepository.save(object);

        ApplicationProperties.OAuth2WebClientInfo clientInfor = applicationProperties.getOauth2WebClientInfoById(object.getAppId());
        ObjectResponseDTO objectResponse = ObjectResponseDTO.builder()
                .key(object.getId())
                .title(object.getName())
                .children(new ArrayList<>())
                .code(clientInfor.getClientId())
                .build();

        //Xóa phân quyền trong API_GROUP_ACL
        apiGroupAclRepository.deleteByObjectId(object.getId());

        //Delete old list
        objectActionRepository.deleteByObjectIdAndActionIdNotIn(object.getId(), authorizedActionIds);
        if (!ObjectUtils.isEmpty(request.getActionIds())) {
            saveListObjectAction(objectResponse, request.getActionIds(), actionIdMap, authorizedActionIds);
        }

        return objectResponse;
    }

    private Set<AuthorizedObjectActionProjectionDTO> checkDeleteActionOfObject(String objectId, List<String> actionIds) {
        Set<String> newActionIds = actionIds == null ? new HashSet<>() : new HashSet<>(actionIds);

        //Lấy danh sách các action đã được phân quyền của Object
        List<AuthorizedObjectActionProjectionDTO> authorizedAction = objectActionRepository.getAuthorizedObjectActionByObjectId(objectId);

        //Kiểm tra xem có action nào được phân quyền bị xoá hay không?
        for(AuthorizedObjectActionProjectionDTO action: authorizedAction){
            if(!newActionIds.contains(action.getActionId())){
                throw new BusinessException(String.format(CAN_NOT_DELETE_AUTHORIZED_ACTION, action.getActionName()));
            }
        }

        return new HashSet<>(authorizedAction);
    }

    @Override
    @Transactional
    public DeleteCountDTO delete(String id, boolean isPartner, Boolean isMobile) {
        String appId = deviceService.getWebClientInfo(isPartner, isMobile).getId();
        int count = 1;
        ObjectEntity findToDelete = objectRepository.findByIdAndAppId(id, appId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.OBJECT_NOT_FOUND));

        //Kiểm tra xem có action nào đã được phân quyền hay chưa
        if(apiCatalogAclRepository.checkObjectIsAuthorized(id)){
            throw new BusinessException(CAN_NOT_DELETE_AUTHORIZED_OBJECT);
        }

        //Delete action of children
        List<ObjectEntity> childrenObjects = objectRepository.findByParentId(findToDelete.getId());
        childrenObjects.forEach(object -> objectActionRepository.deleteAllByObjectId(object.getId()));
        objectRepository.deleteAll(childrenObjects);
        count += childrenObjects.size();

        //Delete action of father
        objectActionRepository.deleteAllByObjectId(findToDelete.getId());
        objectRepository.deleteById(id);
        return new DeleteCountDTO(count);
    }

    @Override
    public ObjectResponseDTO findById(String id, boolean isPartner, Boolean isMobile) {
        String appId = deviceService.getWebClientInfo(isPartner, isMobile).getId();
        ObjectEntity objectEntity = objectRepository.findByIdAndAppId(id, appId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.OBJECT_NOT_FOUND));
        return ObjectResponseDTO.builder()
                .key(objectEntity.getId())
                .title(objectEntity.getName())
                .uri(objectEntity.getUrl())
                .parentId(objectEntity.getParentId())
                .ordinal(objectEntity.getOrdinal())
                .code(objectEntity.getCode())
                .children(findChildrenObject(objectEntity.getId(), appId))
                .build();
    }

    private List<ObjectResponseDTO> findChildrenObject(String objectId, String appId) {
        List<ActionEntity> actionEntities = this.actionRepository.findByJoinWithObject(objectId, appId);
        return actionEntities.stream().map(actionEntity -> ObjectResponseDTO.builder()
                        .key(actionEntity.getId())
                        .title(actionEntity.getName())
                        .code(actionEntity.getCode())
                        .build())
                .toList();

    }



    @Override
    @Transactional(readOnly = true)
    public List<ObjectResponseDTO> tree(boolean isPartner, Boolean isMobile, HttpHeaders headers) {
        log.debug("Get list objects");

        String appCode = deviceService.getWebClientInfo(isPartner, isMobile).getClientId();
        List<ObjectResponseDTO> flatObjectList = this.objectRepository.getObjectHierarchy(appCode);

        if(isPartner){
            List<ObjectResponseDTO> mobileObjects = objectRepository.getObjectHierarchy(applicationProperties.getSaleAppOAuth2ClientInfo().getClientId());
            if(deviceService.getIsMobile(headers) || Boolean.TRUE.equals(isMobile)){
                flatObjectList = mobileObjects;
            }else if(isMobile == null){
                flatObjectList = new ArrayList<>(flatObjectList);
                flatObjectList.addAll(mobileObjects);
            }
        }

        Map<String, ActionEntity> actionCodeMap = this.actionRepository.findAll()
                .stream()
                .distinct()
                .collect(Collectors.toMap(ActionEntity::getCode, e -> e));

        Map<String, ObjectResponseDTO> treeObjectMap = new HashMap<>();
        List<ObjectResponseDTO> treeObjectList = new ArrayList<>();
        Set<String> objectKeySet = new HashSet<>();
        for (ObjectResponseDTO object : flatObjectList) {
            this.transformToTree(object, treeObjectMap, actionCodeMap, objectKeySet, treeObjectList);
        }
        return treeObjectList;
    }

    private void transformToTree(ObjectResponseDTO object, Map<String, ObjectResponseDTO> treeObjectMap, Map<String, ActionEntity> actionCodeMap, Set<String> objectKeySet, List<ObjectResponseDTO> treeObjectList) {
        ObjectResponseDTO temp;
        if (object.getChildren() == null) {
            object.setChildren(new ArrayList<>());
        }

        temp = object;
        if (treeObjectMap.containsKey(object.getKey())) {
            object = treeObjectMap.get(object.getKey());
        }
        if (StringUtils.hasText(temp.getJoinedActions())) {
            String[] actionCodes = temp.getJoinedActions().split(",");
            for (String actionCode : actionCodes) {
                ActionEntity actionEntity = actionCodeMap.get(actionCode);
                if (actionEntity == null) continue;
                object.getChildren().add(ObjectResponseDTO.builder()
                        .key(temp.getKey() + AuthConstants.CommonSymbol.DASH + actionEntity.getId())
                        .title(actionEntity.getName()).build());
            }
        }

        treeObjectMap.put(object.getKey(), object);

        if (object.getParentId() == null && objectKeySet.add(object.getKey())) {
            // root item
            treeObjectList.add(object);
            return;
        }

        ObjectResponseDTO parentItem = treeObjectMap.get(object.getParentId());
        if (parentItem != null && objectKeySet.add(object.getKey())) {
            parentItem.getChildren().add(object);
        }
    }

    @Override
    public UserPolicyDTO retrieveUserPolicy(String userId, String appCode) {
        String cacheKey = String.format("%s::%s", userId, appCode);
        Map<String, Set<String>> cachedPolicyMap;
        if (this.userPolicyCacheOn) {
            cachedPolicyMap = userPolicyCache.get(cacheKey, Map.class);
            if (Objects.nonNull(cachedPolicyMap)) {
                log.info("[USER_POLICY_V2] With id={}, appCode={} => cache hit={}", userId, appCode, true);
                return UserPolicyDTO.builder()
                        .appCode(appCode)
                        .userId(userId)
                        .resourceAccess(cachedPolicyMap)
                        .build();
            }
        }
        cachedPolicyMap = this.objectRepository.getUserPermissionList(userId, appCode);


        String partnerAppCode = applicationProperties.getPartnerWebOAuth2ClientInfo().getClientId();
        if(Objects.equals(appCode, partnerAppCode)){
            String skySaleAppCode = applicationProperties.getSaleAppOAuth2ClientInfo().getClientId();
            cachedPolicyMap.putAll(objectRepository.getUserPermissionList(userId, skySaleAppCode));
        }
        UserPolicyDTO userPolicyDTO = UserPolicyDTO.builder()
                .appCode(appCode)
                .userId(userId)
                .resourceAccess(cachedPolicyMap)
                .build();
        log.info("[USER_POLICY_V2] With id={}, appCode={} => cache hit={}", userId, appCode, false);
        return userPolicyDTO;
    }

    @Override
    public ApiPolicyDTO retrieveApiPolicy(ApiPolicyDTO apiPolicy) {
        String cacheKey = String.format("%s::%s::%s",
                apiPolicy.getServiceCode(),
                apiPolicy.getApiUriPattern(),
                apiPolicy.getApiMethod()
        );
        ApiPolicyDTO apiAclResponse;
        if (this.apiAclCacheOn) {
            apiAclResponse = apiPolicyCache.get(cacheKey, ApiPolicyDTO.class);
            if (apiAclResponse != null) {
                log.info("[API_POLICY_V2] With serviceCode={}, uriPattern={}, method={} => cache hit={}",
                        apiPolicy.getServiceCode(), apiPolicy.getApiUriPattern(), apiPolicy.getApiMethod(), true);
                return apiAclResponse;
            }
        }

        Optional<ApiCatalogEntity> optionalApiCatalogEntity = this.apiCatalogRepository.findByServiceCodeAndUriPatternAndMethod(
                apiPolicy.getServiceCode(),
                apiPolicy.getApiUriPattern(),
                apiPolicy.getApiMethod()
        );
        if (optionalApiCatalogEntity.isEmpty()) {
            return ApiPolicyDTO.builder()
                    .serviceCode(apiPolicy.getServiceCode())
                    .apiUriPattern(apiPolicy.getApiUriPattern())
                    .apiMethod(apiPolicy.getApiMethod())
                    .permissions(List.of())
                    .isIgnore(true)
                    .build();
        }
        ApiCatalogEntity apiCatalogEntity = optionalApiCatalogEntity.get();
        if (!Objects.equals(AuthConstants.ModelStatus.ACTIVE, apiCatalogEntity.getStatus())) {
            return ApiPolicyDTO.builder()
                    .serviceCode(apiCatalogEntity.getServiceCode())
                    .apiUriPattern(apiCatalogEntity.getUriPattern())
                    .apiMethod(apiCatalogEntity.getMethod())
                    .permissions(List.of())
                    .isIgnore(true)
                    .build();
        }
        List<PermissionDTO> permissions = new ArrayList<>();
        this.objectActionRepository
                .findByApiCatalogId(apiCatalogEntity.getId())
                .forEach(apiAcl -> permissions.add(PermissionDTO.builder()
                        .objectCode(apiAcl.getObjectCode())
                        .actionCode(apiAcl.getActionCode())
                        .build()));
        apiAclResponse = ApiPolicyDTO.builder()
                .serviceCode(apiCatalogEntity.getServiceCode())
                .apiUriPattern(apiCatalogEntity.getUriPattern())
                .apiMethod(apiCatalogEntity.getMethod())
                .permissions(permissions)
                .build();
        apiPolicyCache.put(cacheKey, apiAclResponse);
        log.info("[API_POLICY_V2] With serviceCode={}, uriPattern={}, method={} => cache hit={}",
                apiPolicy.getServiceCode(), apiPolicy.getApiUriPattern(), apiPolicy.getApiMethod(), false);
        return apiAclResponse;
    }

    @Async
    @Override
    public void clearUserPolicyCache(Set<String> userIds, String appCode) {
        this.redisOperations.executePipelined((RedisCallback<Object>) connection -> {
            DataUtil.batchProcess(userIds, ids -> {
                byte[][] keys = ids
                        .stream()
                        .map(id ->
                                (CacheKey.CACHE_KEY_PREFIX + CacheKey.USER_POLICY_PREFIX + CacheKey.CACHE_KEY_SEPARATOR + id).getBytes(StandardCharsets.UTF_8))
                        .toArray(byte[][]::new);
                connection.keyCommands().unlink(keys);
                log.info("[CLEAR_USER_POLICY_CACHE] Unlink batch of {} keys", ids.size());
            }, 10);
            return null;
        });
    }

    @Override
    public PolicyCacheConfigDTO getCacheConfig() {
        return PolicyCacheConfigDTO.builder()
                .userInfoCacheOn(this.userInfoCacheOn)
                .apiAclCacheOn(this.apiAclCacheOn)
                .userPolicyCacheOn(this.userPolicyCacheOn)
                .build();
    }

    @Override
    public PolicyCacheConfigDTO updateCacheConfig(PolicyCacheConfigDTO policyCacheConfig) {
        this.userInfoCacheOn = Boolean.TRUE.equals(policyCacheConfig.getUserInfoCacheOn());
        this.apiAclCacheOn = Boolean.TRUE.equals(policyCacheConfig.getApiAclCacheOn());
        this.userPolicyCacheOn = Boolean.TRUE.equals(policyCacheConfig.getUserPolicyCacheOn());
        return PolicyCacheConfigDTO.builder()
                .userInfoCacheOn(this.userInfoCacheOn)
                .apiAclCacheOn(this.apiAclCacheOn)
                .userPolicyCacheOn(this.userPolicyCacheOn)
                .build();
    }

    @Override
    public boolean checkPolicy(Map<String, Set<String>> policyMap, List<PermissionDTO> permissions) {
        for (PermissionDTO apiPermission : permissions) {
            Set<String> matchedActions = policyMap.get(apiPermission.getObjectCode());
            if (matchedActions != null &&
                (WILDCARD_ACTION.equals(apiPermission.getActionCode()) || matchedActions.contains(apiPermission.getActionCode()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Nullable
    public UserDTO refreshUserInfoCache(String userId) {
        Optional<UserEntity> optionalUserEntity = this.userRepository.findById(userId);
        if (optionalUserEntity.isPresent()) {
            UserDTO userDTO = this.userMapper.toDto(optionalUserEntity.get());
            userInfoCache.put(userId, userDTO);
            return userDTO;
        } else {
            userInfoCache.evict(userId);
            return null;
        }
    }

    @Nullable
    private UserDTO getUserInfo(String userId) {
        UserDTO user;
        if (this.userInfoCacheOn) {
            user = userInfoCache.get(userId, UserDTO.class);
            if (Objects.nonNull(user)) {
                log.info("[USER_INFO] With id={} => cache hit={}", userId, true);
                return user;
            }
        }
        user = this.refreshUserInfoCache(userId);
        log.info("[USER_INFO] With id={} => cache hit={}", userId, false);
        return user;
    }

    @Override
    public PolicyCheckDTO checkPolicy(PolicyCheckDTO policyCheck) {
        UserDTO user = this.getUserInfo(policyCheck.getUserId());
        if (Objects.isNull(user)) {
            return PolicyCheckDTO.builder()
                    .granted(false)
                    .build();
        }
        ApiPolicyDTO apiPolicyDTO1 = ApiPolicyDTO.builder()
                .serviceCode(policyCheck.getServiceCode())
                .apiUriPattern(policyCheck.getUriPattern())
                .apiMethod(policyCheck.getMethod())
                .build();
        ApiPolicyDTO apiPolicyResponse = this.retrieveApiPolicy(apiPolicyDTO1);
        if (apiPolicyResponse.isIgnore()) return PolicyCheckDTO.builder()
                .user(user)
                .granted(true)
                .build();
        String appCode = policyCheck.getAppCode();
        UserPolicyDTO userPolicy = this.retrieveUserPolicy(policyCheck.getUserId(), appCode);
        for (PermissionDTO apiPermission : apiPolicyResponse.getPermissions()) {
            Set<String> matchedActions = userPolicy.getResourceAccess().get(apiPermission.getObjectCode());
            if (matchedActions != null && matchedActions.contains(apiPermission.getActionCode())) {
                return PolicyCheckDTO.builder()
                        .user(user)
                        .matchedObjectCode(apiPermission.getObjectCode())
                        .matchedActionCode(apiPermission.getActionCode())
                        .granted(true)
                        .build();
            }
        }
        return PolicyCheckDTO.builder()
                .user(user)
                .granted(false)
                .build();
    }

    @Override
    @Transactional
    public void registerSelfApiSet() {
        RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext
                .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> mapHandler = requestMappingHandlerMapping
                .getHandlerMethods();
        this.apiCatalogRepository.markForDeletion(this.serviceCode);
        mapHandler.forEach((key, value) -> {
            String controllerMethod = String.format("%s.%s", value.getBeanType(), value.getMethod().getName());
            String fmtMethod = key.getMethodsCondition().toString();
            String rawMethod = fmtMethod.substring(1, fmtMethod.length() - 1);
            key.getPatternValues().forEach(pattern -> {
                log.info("[API_CATALOG_REGISTER] URI Pattern: {}, Method {}, Controller Method: {}", pattern, fmtMethod, controllerMethod);
                if (!StringUtils.hasText(rawMethod)) return;
                ApiCatalogEntity apiCatalogEntity = ApiCatalogEntity.builder()
                        .id(UlidCreator.getUlid().toString())
                        .name(controllerMethod)
                        .serviceCode(this.serviceCode)
                        .uriPattern(pattern)
                        .method(rawMethod)
                        .status(AuthConstants.ModelStatus.INACTIVE)
                        .build();
                this.apiCatalogRepository.upsert(apiCatalogEntity);
            });
        });
    }

    @Override
    @Transactional
    public void registerApiSet(ApiCatalogRegistrationDTO apiCatalogRegistration) {
        this.apiCatalogRepository.markForDeletion(apiCatalogRegistration.getServiceCode());
        this.apiCatalogMapper.toEntity(apiCatalogRegistration.getApiCatalogs()).forEach(apiCatalogEntity -> {
            apiCatalogEntity.setId(UlidCreator.getUlid().toString());
            apiCatalogEntity.setServiceCode(apiCatalogRegistration.getServiceCode());
            log.info("[API_CATALOG_REGISTER] URI Pattern: {}, Method {}, Controller Method: {}", apiCatalogEntity.getUriPattern(), apiCatalogEntity.getMethod(), apiCatalogEntity.getName());
            this.apiCatalogRepository.upsert(apiCatalogEntity);
        });
    }

    @Transactional
    public void updateObjectActionName() {
        List<ObjectActionEntity> objectActionEntities = this.objectActionRepository.findAll();
        for (ObjectActionEntity objectActionEntity : objectActionEntities) {
            this.objectRepository.findById(objectActionEntity.getObjectId()).ifPresent(objectEntity -> {
                String appCode;
                if (this.applicationProperties.getVnskyWebOAuth2ClientInfo().getId().equals(objectEntity.getAppId())) {
                    appCode = this.applicationProperties.getVnskyWebOAuth2ClientInfo().getClientId();
                } else if (this.applicationProperties.getPartnerWebOAuth2ClientInfo().getId().equals(objectEntity.getAppId())) {
                    appCode = this.applicationProperties.getPartnerWebOAuth2ClientInfo().getClientId();
                } else if (this.applicationProperties.getThirdPartyOAuth2ClientInfo().getId().equals(objectEntity.getAppId())) {
                    appCode = this.applicationProperties.getThirdPartyOAuth2ClientInfo().getClientId();
                } else {
                    appCode = "";
                }
                this.actionRepository.findById(objectActionEntity.getActionId()).ifPresent(actionEntity -> {
                    String aclName = appCode + AuthConstants.CommonSymbol.FORWARD_SLASH + objectEntity.getCode() + AuthConstants.CommonSymbol.FORWARD_SLASH + actionEntity.getCode();
                    objectActionEntity.setName(aclName);
                });
            });
            this.objectActionRepository.saveAndFlush(objectActionEntity);
        }
        log.info("Update ObjectAction done!");
    }

    @Transactional
    @Override
    public Resource uploadAclPolicy(MultipartFile file) {
        List<UploadAclPolicyRequestDTO> uploadingData = readUploadData(file);
        List<ApiCatalogAclEntity> aclEntities = new ArrayList<>();
        List<String> updatingStatusApis = new ArrayList<>();

        for (UploadAclPolicyRequestDTO data : uploadingData) {
            try {
                Tuple tuple = apiCatalogAclRepository.findForUploadAcl(data);
                ApiCatalogAclDTO findForValidate = dbMapper.castSqlResult(tuple, ApiCatalogAclDTO.class);
                checkApiCatalogExist(findForValidate);
                checkObjectActionExist(findForValidate);
                checkApiCatalogAclExist(findForValidate);

                aclEntities.add(ApiCatalogAclEntity.builder()
                        .aclID(findForValidate.getObjectActionId())
                        .catalogId(findForValidate.getApiCatalogId())
                        .build());

                //Cập nhật trạng thái apiCatalog thành 1 để phân quyền có tác dụng
                updatingStatusApis.add(findForValidate.getApiCatalogId());
            } catch (Exception e) {
                data.setResult(e.getMessage());
            }
        }
        apiCatalogAclRepository.saveAllAndFlush(aclEntities);

        final int batchSize = 200;
        for(int index = 0; index < updatingStatusApis.size(); index += batchSize){
            List<String> batchUpdatingStatusApis = updatingStatusApis.subList(index, Math.min(updatingStatusApis.size(), index + batchSize));
            apiCatalogRepository.updateStatusToActiveByIds(batchUpdatingStatusApis);
        }

        return xlsxOperations.writeExcel(new ExcelData<>(new HashMap<>(), uploadingData, false ), UploadAclPolicyRequestDTO.class, true);
    }

    private void checkApiCatalogExist(ApiCatalogAclDTO apiCatalog){
        if(apiCatalog == null || apiCatalog.getApiCatalogId() == null){
            throw BaseException.badRequest(ErrorKey.BAD_REQUEST).message(API_CATALOG_NOT_FOUND).build();
        }
    }

    private void checkObjectActionExist(ApiCatalogAclDTO apiCatalog){
        if(apiCatalog == null || apiCatalog.getObjectActionId() == null){
            throw BaseException.badRequest(ErrorKey.BAD_REQUEST).message(OBJECT_ACTION_NOT_FOUND).build();
        }
    }

    private void checkApiCatalogAclExist(ApiCatalogAclDTO apiCatalog){
        if(apiCatalog.getAclId() != null && apiCatalog.getCatalogId() != null){
            throw BaseException.badRequest(ErrorKey.BAD_REQUEST).message(EXISTED_API_CATALOG_ACL).build();
        }
    }

    private List<UploadAclPolicyRequestDTO> readUploadData(MultipartFile file) {
        try {
            ExcelData<UploadAclPolicyRequestDTO> readingData = xlsxOperations.readExcelWithoutThrow(file.getInputStream(), UploadAclPolicyRequestDTO.class);
            return readingData.getDataLines();
        } catch (IOException e) {
            throw BaseException.badRequest(ErrorKey.BAD_REQUEST).addProblemDetail(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Lỗi khi đọc file excel")).build();
        }
    }
}
