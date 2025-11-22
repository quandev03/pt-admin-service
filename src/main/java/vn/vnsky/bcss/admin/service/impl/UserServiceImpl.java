package vn.vnsky.bcss.admin.service.impl;

import com.vnsky.common.exception.domain.BaseException;
import com.vnsky.common.exception.domain.ErrorKey;
import jakarta.persistence.EntityNotFoundException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.constant.*;
import vn.vnsky.bcss.admin.dto.*;
import vn.vnsky.bcss.admin.entity.*;
import vn.vnsky.bcss.admin.error.BusinessException;
import vn.vnsky.bcss.admin.error.FieldsValidationException;
import vn.vnsky.bcss.admin.mapper.ClientMapper;
import vn.vnsky.bcss.admin.mapper.UserMapper;
import vn.vnsky.bcss.admin.repository.*;
import vn.vnsky.bcss.admin.service.*;
import vn.vnsky.bcss.admin.util.RandomUtil;
import vn.vnsky.bcss.admin.util.RequestUtil;
import vn.vnsky.bcss.admin.util.SecurityUtil;
import vn.vnsky.bcss.admin.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private static final String VNSKY_CLIENT_ID = "000000000000";

    protected static final String ACCESS_LOG_QUEUE = "system-access-log";

    private static final String USER_SIGNATURE_PATH = "USER_SIGNATURE";

    private static final String USER_SIGNATURE_EXT = "png";

    private static final int USER_SIGNATURE_MAX_WIDTH = 1024;

    private static final int USER_SIGNATURE_MAX_HEIGHT = 768;

    private final ApplicationProperties applicationProperties;

    private final ClientRepository clientRepository;

    private final UserRepository userRepository;

    private final GroupRepository groupRepository;

    private final RoleRepository roleRepository;

    private final DepartmentRepository departmentRepository;

    private final ObjectRepository objectRepository;

    private final FcmUserTokenRepository fcmUserTokenRepository;

    private final ParamRepository paramRepository;

    private final ClientMapper clientMapper;

    private final UserMapper userMapper;

    private final StorageService storageService;

    private final ObjectService objectService;

    private final PasswordEncoder passwordEncoder;

    private final Cache forgotPasswordCache;

    private final MailService mailService;

    private final KafkaOperations<String, Object> kafkaOperations;

    private final RestTemplate restTemplate;

    private final DeviceService deviceService;

    private static final String API_KEY="api-key-hi-viet-nam";
    private static final String URL_CHECK_PARENT="/private/api/v1/organization-unit/check-org-parent";
    private static final String URL_CREATE_USER_PARTNER="/private/api/v1/organization-user";

    @Value("${domain.internal.domain-sale}")
    private String baseUrl;

    @Autowired
    public UserServiceImpl(ApplicationProperties applicationProperties,
                           ClientRepository clientRepository, UserRepository userRepository,
                           GroupRepository groupRepository, RoleRepository roleRepository,
                           DepartmentRepository departmentRepository, ObjectRepository objectRepository,
                           FcmUserTokenRepository fcmUserTokenRepository, ParamRepository paramRepository,
                           ClientMapper clientMapper, UserMapper userMapper,
                           PasswordEncoder passwordEncoder, CacheManager cacheManager,
                           MailService mailService, StorageService storageService,
                           ObjectService objectService, KafkaOperations<String, Object> kafkaOperations,
                           DeviceService deviceService, RestTemplate restTemplate) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.objectRepository = objectRepository;
        this.fcmUserTokenRepository = fcmUserTokenRepository;
        this.paramRepository = paramRepository;
        this.clientMapper = clientMapper;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.applicationProperties = applicationProperties;
        this.storageService = storageService;
        this.objectService = objectService;
        this.kafkaOperations = kafkaOperations;
        this.forgotPasswordCache = Objects.requireNonNull(cacheManager.getCache(CacheKey.USER_FORGOT_PASSWORD_TOKEN_PREFIX));
        this.deviceService = deviceService;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO loadUserByClientAndUser(String clientId, String username) {
        String fmtUsername = username.toLowerCase();
        return this.userRepository.findByClientIdAndUsername(clientId, fmtUsername)
                .map(this.userMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean lockUserForFailedLoginAttempts(UserDTO userDTO) {
        boolean locked = false;
        Optional<UserEntity> userEntityOptional = this.userRepository.findById(userDTO.getId());
        if (userEntityOptional.isPresent()) {
            UserEntity userEntity = userEntityOptional.get();
            if (userEntity.getLoginFailedCount() == null) {
                userEntity.setLoginFailedCount(0);
            }
            userEntity.setLoginFailedCount(userEntity.getLoginFailedCount() + 1);
            if (userEntity.getLoginFailedCount() > 3) {
                userEntity.setStatus(UserStatus.LOCKED.getValue());
                locked = true;
            }
            this.userRepository.saveAndFlush(userEntity);
        }
        return locked;
    }

    @Override
    @Transactional
    public void resetUserFailedLoginAttempts(UserDTO userDTO) {
        this.userRepository.findById(userDTO.getId())
                .ifPresent(userEntity -> {
                    if (UserStatus.ACTIVE.getValue().equals(userEntity.getStatus())) {
                        userEntity.setLoginFailedCount(0);
                    }
                    this.userRepository.saveAndFlush(userEntity);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> search(String clientIdentity, String term, Integer status, Pageable pageable) {
        ClientEntity clientEntity = this.clientRepository.findByIdOrAlias(clientIdentity)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.CLIENT_NOT_FOUND));
        String clientId = clientEntity.getId();
        String fmtTerm = StringUtil.buildLikeOperatorLower(term);

        List<Integer> statuses = new ArrayList<>();
        if(Objects.equals(status, UserStatus.ACTIVE.getValue())){
            statuses = List.of(UserStatus.ACTIVE.getValue());
        }else if(Objects.equals(status, UserStatus.INACTIVE.getValue())){
            statuses = List.of(UserStatus.INACTIVE.getValue(), UserStatus.LOCKED.getValue());
        }
        return this.userRepository.findByTerm(fmtTerm, statuses, clientId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> all(String clientIdentity, List<String> permissions, String departmentCode, Boolean isActive, String sort) {
        String appId = SecurityUtil.getCurrentAppId();
        ClientEntity clientEntity = this.clientRepository.findByIdOrAlias(clientIdentity)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.CLIENT_NOT_FOUND));
        if (!ObjectUtils.isEmpty(permissions)) {
            return this.filterByPermissions(permissions, appId, clientEntity)
                    .stream()
                    .filter(e -> isActive == null || Objects.equals(UserStatus.ACTIVE.getValue(), e.getStatus()) == isActive)
                    .toList();

        } else if (StringUtils.hasText(departmentCode)) {
            return this.userRepository.findAllByClientIdAndDepartmentCode(clientEntity.getId(), departmentCode)
                    .stream()
                    .filter(e -> isActive == null || Objects.equals(UserStatus.ACTIVE.getValue(), e.getStatus()) == isActive)
                    .toList();
        } else {
            return this.userRepository.findAllByClientOrderByLastModifiedDateDescCreatedDateDesc(clientEntity)
                    .stream()
                    .filter(e -> isActive == null || Objects.equals(UserStatus.ACTIVE.getValue(), e.getStatus()) == isActive)
                    .map(this.userMapper::toDtoPure)
                    .toList();
        }
    }

    private List<UserDTO> filterByPermissions(List<String> permissions, String appId, ClientEntity clientEntity) {
        Map<String, Set<String>> objectActionMap = new HashMap<>();
        for (String permission : permissions) {
            String[] roleObjectArr = permission.split(AuthConstants.CommonSymbol.COLON);
            String objectCode = roleObjectArr[0];
            String actionCode = roleObjectArr[1];
            if (AuthConstants.CommonSymbol.ASTERISK.equals(actionCode)) {
                objectActionMap.put(objectCode, null);
                continue;
            }
            if (objectActionMap.containsKey(objectCode)) {
                Set<String> actionCodes = objectActionMap.get(objectCode);
                if (actionCodes != null) {
                    actionCodes.add(actionCode);
                }
            } else {
                Set<String> actionCodes = new HashSet<>();
                actionCodes.add(actionCode);
                objectActionMap.put(objectCode, actionCodes);
            }
        }
        List<ObjectActionDTO> objectActions = this.objectRepository.findAllObjectActionByObjectCodes(appId, objectActionMap.keySet());
        for (ObjectActionDTO objectAction : objectActions) {
            Set<String> actionCodes = objectActionMap.get(objectAction.getObjectCode());
            if (actionCodes == null || actionCodes.contains(objectAction.getActionCode())) {
                this.objectRepository.insertCurrentObjectAction(objectAction.getObjectId(), objectAction.getActionId());
            }
        }
        int count = this.objectRepository.countCurrentObjectAction();
        log.info("Current object action count = {}", count);
        return this.userRepository.findAllByClientIdAndPermissions(clientEntity.getId(), permissions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> all(Boolean isPartner, String term) {
        return this.userRepository.findAllByClientType(isPartner, term);
    }

    @Override
    @Transactional
    public UserDTO create(String clientIdentity, UserDTO userDTO) {
        userDTO.setType(null);
        userDTO.setUsername(userDTO.getUsername().toLowerCase());
        userDTO.setEmail(userDTO.getEmail().toLowerCase());
        Map<String, String> fieldErrors = new HashMap<>();
        this.checkExisted(userDTO);
        if (ObjectUtils.isEmpty(userDTO.getGroupIds()) && ObjectUtils.isEmpty(userDTO.getRoleIds()))
            throw new BusinessException(ErrorMessageConstant.REQUIRED_ROLE_GROUP);
        String username = userDTO.getUsername();
        if (UserLoginMethod.LOGIN_GOOGLE.getValue() == userDTO.getLoginMethod() && !username.equalsIgnoreCase(userDTO.getEmail())) {
            fieldErrors.put(UserDTO.Fields.loginMethod, ErrorMessageConstant.INVALID_LOGIN_METHOD);
            throw new FieldsValidationException(fieldErrors);
        }
        ClientEntity clientEntity = this.clientRepository.findByIdOrAlias(clientIdentity)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.CLIENT_NOT_FOUND));
        UserEntity userEntity = this.userMapper.toEntity(userDTO);
        this.userRepository.findByClientIdAndUsername(clientEntity.getId(), username)
                .ifPresent(existedUserEntity -> {
                    fieldErrors.put(UserDTO.Fields.username, ErrorMessageConstant.USER_NAME_EXIST);
                    throw new FieldsValidationException(fieldErrors);
                });
        this.userRepository.findByClientIdAndEmail(clientEntity.getId(), userDTO.getEmail())
                .ifPresent(existedUserEntity -> {
                    fieldErrors.put(UserDTO.Fields.email, ErrorMessageConstant.EMAIL_EXIST);
                    throw new FieldsValidationException(fieldErrors);
                });
        userEntity.setClient(clientEntity);
        this.userRepository.saveAndFlush(userEntity);
        this.persistRoles(userDTO, userEntity);
        this.persistGroups(userDTO, userEntity);
        this.persistDepartments(userDTO, userEntity);
        String newPwd = StringUtil.generateRandomPassword();
        String webUrl = AuthConstants.VNSKY_CLIENT_ID.equals(clientEntity.getId()) ?
                this.applicationProperties.getVnskyWebOAuth2ClientInfo().getUrl() :
                this.applicationProperties.getPartnerWebOAuth2ClientInfo().getUrl();
        log.info("Activate account web url: {}", webUrl);
        MailInfoDTO mailInfo = MailInfoDTO.builder()
                .expireTime(AuthConstants.EmailContent.MAIL_EXPIRE_TIME)
                .subject(AuthConstants.EmailContent.MAIL_SUBJECT_ACTIVATE)
                .to(StringUtils.hasText(userDTO.getEmail()) ? userDTO.getEmail() : username)
                .content(clientEntity.getCode())
                .username(username)
                .password(newPwd)
                .companyName(paramRepository.getCompanyName())
                .url(webUrl)
                .build();
        this.mailService.sendTokenActivateAccount(mailInfo);
        userEntity.setPassword(this.passwordEncoder.encode(newPwd));
        userEntity.setPasswordExpireTime(LocalDateTime.now());
        userEntity.setStatus(AuthConstants.ModelStatus.ACTIVE);
        userEntity = this.userRepository.saveAndFlush(userEntity);
        userDTO.setId(userEntity.getId());
        userDTO = this.userMapper.convertEntity2DtoFull(userEntity);
        this.objectService.refreshUserInfoCache(userEntity.getId());
        return userDTO;
    }

    @Override
    @Transactional
    public UserDTO createPartner( String orgId , UserDTO userDTO) {

        String clientId = SecurityUtil.getCurrentClientId();
        log.info("Create partner with client id = {}", clientId);

        log.info("check parent id");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-api-key", API_KEY );
        headers.add(HttpHeaders.ACCEPT_LANGUAGE, "en");

        CheckOrgParentRequest checkOrgParentRequest = CheckOrgParentRequest.builder()
                .clientId(clientId)
                .orgId(orgId)
                .currentUserId(SecurityUtil.getCurrentUserId())
                .build();

        HttpEntity<Object> requestEntity = new HttpEntity<>(checkOrgParentRequest, headers);
        String urlCheck = baseUrl + URL_CHECK_PARENT;
        CheckOrgParentResponse responseCheck;
        try {
            responseCheck = restTemplate.exchange(urlCheck, HttpMethod.POST, requestEntity, CheckOrgParentResponse.class).getBody();
            log.info("check parent response: {}", responseCheck);
            assert responseCheck != null;
            if(Objects.equals(responseCheck.getResult(), 0)){
                throw BaseException.badRequest(ErrorKey.BAD_REQUEST).build();
            }
        } catch (HttpClientErrorException ex) {
            log.error("Error calling check-org-parent API: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            String errorMessage = ex.getResponseBodyAsString();
            String extractedMessage = null;
            
            if (StringUtils.hasText(errorMessage)) {
                try {
                    // Try to extract detail field from JSON response
                    if (errorMessage.contains("\"detail\"")) {
                        int detailStart = errorMessage.indexOf("\"detail\"");
                        int colonIndex = errorMessage.indexOf(":", detailStart);
                        if (colonIndex > 0) {
                            int valueStart = errorMessage.indexOf("\"", colonIndex) + 1;
                            if (valueStart > 0) {
                                int valueEnd = errorMessage.indexOf("\"", valueStart);
                                if (valueEnd > valueStart) {
                                    extractedMessage = errorMessage.substring(valueStart, valueEnd);
                                }
                            }
                        }
                    }
                    
                    // If extracted message contains template placeholders, remove them
                    if (extractedMessage != null && extractedMessage.contains("{{")) {
                        extractedMessage = extractedMessage.replaceAll("\\{\\{[^}]*\\}\\}", "").trim();
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse error message: {}", e.getMessage());
                }
            }
            
            // Use extracted message if valid, otherwise use default
            if (StringUtils.hasText(extractedMessage) && !extractedMessage.contains("{{")) {
                throw new BusinessException(extractedMessage);
            } else {
                throw new BusinessException(ErrorMessageConstant.BAD_REQUEST);
            }
        } catch (RestClientException ex) {
            log.error("Rest client error calling check-org-parent API: {}", ex.getMessage(), ex);
            throw new BusinessException(ErrorMessageConstant.BAD_REQUEST);
        }

        userDTO.setType(null);
        userDTO.setUsername(userDTO.getUsername().toLowerCase());
        userDTO.setEmail(userDTO.getEmail().toLowerCase());
        Map<String, String> fieldErrors = new HashMap<>();
        this.checkExisted(userDTO);
        if (ObjectUtils.isEmpty(userDTO.getGroupIds()) && ObjectUtils.isEmpty(userDTO.getRoleIds()))
            throw new BusinessException(ErrorMessageConstant.REQUIRED_ROLE_GROUP);
        String username = userDTO.getUsername();
        if (UserLoginMethod.LOGIN_GOOGLE.getValue() == userDTO.getLoginMethod() && !username.equalsIgnoreCase(userDTO.getEmail())) {
            fieldErrors.put(UserDTO.Fields.loginMethod, ErrorMessageConstant.INVALID_LOGIN_METHOD);
            throw new FieldsValidationException(fieldErrors);
        }
        ClientEntity clientEntity = this.clientRepository.findByIdOrAlias(clientId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.CLIENT_NOT_FOUND));
        UserEntity userEntity = this.userMapper.toEntity(userDTO);
        this.userRepository.findByClientIdAndUsername(clientEntity.getId(), username)
                .ifPresent(existedUserEntity -> {
                    fieldErrors.put(UserDTO.Fields.username, ErrorMessageConstant.USER_NAME_EXIST);
                    throw new FieldsValidationException(fieldErrors);
                });
        this.userRepository.findByClientIdAndEmail(clientEntity.getId(), userDTO.getEmail())
                .ifPresent(existedUserEntity -> {
                    fieldErrors.put(UserDTO.Fields.email, ErrorMessageConstant.EMAIL_EXIST);
                    throw new FieldsValidationException(fieldErrors);
                });
        userEntity.setClient(clientEntity);
        this.userRepository.saveAndFlush(userEntity);
        this.persistRoles(userDTO, userEntity);
        this.persistGroups(userDTO, userEntity);
        this.persistDepartments(userDTO, userEntity);
        String newPwd = StringUtil.generateRandomPassword();
        String webUrl = AuthConstants.VNSKY_CLIENT_ID.equals(clientEntity.getId()) ?
                this.applicationProperties.getVnskyWebOAuth2ClientInfo().getUrl() :
                this.applicationProperties.getPartnerWebOAuth2ClientInfo().getUrl();
        log.info("Activate account web url: {}", webUrl);
        MailInfoDTO mailInfo = MailInfoDTO.builder()
                .expireTime(AuthConstants.EmailContent.MAIL_EXPIRE_TIME)
                .subject(AuthConstants.EmailContent.MAIL_SUBJECT_ACTIVATE)
                .to(StringUtils.hasText(userDTO.getEmail()) ? userDTO.getEmail() : username)
                .content(clientEntity.getCode())
                .username(username)
                .password(newPwd)
                .companyName(paramRepository.getCompanyName())
                .url(webUrl)
                .build();
        this.mailService.sendTokenActivateAccount(mailInfo);
        userEntity.setPassword(this.passwordEncoder.encode(newPwd));
        userEntity.setPasswordExpireTime(LocalDateTime.now());
        userEntity.setStatus(AuthConstants.ModelStatus.ACTIVE);
        userEntity = this.userRepository.saveAndFlush(userEntity);
        userDTO.setId(userEntity.getId());
        userDTO = this.userMapper.convertEntity2DtoFull(userEntity);
        this.objectService.refreshUserInfoCache(userEntity.getId());

        log.info("Create organization user");

        OrganizationUserDTO organizationUserDTO = OrganizationUserDTO.builder()
                .userFullname(userDTO.getFullname())
                .userName(userDTO.getUsername())
                .isCurrent(1)
                .clientId(clientId)
                .email(userDTO.getEmail())
                .orgId(orgId)
                .userId(userDTO.getId())
                .status(1)
                .build();

        String urlCreateOrgUser = baseUrl + URL_CREATE_USER_PARTNER;

        HttpEntity<Object> requestEntityCreateUser = new HttpEntity<>(organizationUserDTO, headers);
        try {
            organizationUserDTO = restTemplate.exchange(urlCreateOrgUser, HttpMethod.POST, requestEntityCreateUser, OrganizationUserDTO.class).getBody();
        } catch (HttpClientErrorException ex) {
            log.error("Error calling create-org-user API: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            String errorMessage = ex.getResponseBodyAsString();
            String extractedMessage = null;
            
            if (StringUtils.hasText(errorMessage)) {
                try {
                    // Try to extract detail field from JSON response
                    if (errorMessage.contains("\"detail\"")) {
                        int detailStart = errorMessage.indexOf("\"detail\"");
                        int colonIndex = errorMessage.indexOf(":", detailStart);
                        if (colonIndex > 0) {
                            int valueStart = errorMessage.indexOf("\"", colonIndex) + 1;
                            if (valueStart > 0) {
                                int valueEnd = errorMessage.indexOf("\"", valueStart);
                                if (valueEnd > valueStart) {
                                    extractedMessage = errorMessage.substring(valueStart, valueEnd);
                                }
                            }
                        }
                    }
                    
                    // If extracted message contains template placeholders, remove them
                    if (extractedMessage != null && extractedMessage.contains("{{")) {
                        extractedMessage = extractedMessage.replaceAll("\\{\\{[^}]*\\}\\}", "").trim();
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse error message: {}", e.getMessage());
                }
            }
            
            // Use extracted message if valid, otherwise use default
            if (StringUtils.hasText(extractedMessage) && !extractedMessage.contains("{{")) {
                throw new BusinessException(extractedMessage);
            } else {
                throw new BusinessException(ErrorMessageConstant.BAD_REQUEST);
            }
        } catch (RestClientException ex) {
            log.error("Rest client error calling create-org-user API: {}", ex.getMessage(), ex);
            throw new BusinessException(ErrorMessageConstant.BAD_REQUEST);
        }

        if( Objects.nonNull(organizationUserDTO) && Objects.isNull(organizationUserDTO.getId())){
            throw new EntityNotFoundException(ErrorMessageConstant.USER_NOT_FOUND);
        }
        return userDTO;
    }

    private void persistGroups(UserDTO userDTO, UserEntity userEntity) {
        Set<GroupEntity> groupsPersist = new HashSet<>();
        if (!CollectionUtils.isEmpty(userDTO.getGroupIds())) {
            ClientEntity clientEntity = userEntity.getClient();
            groupsPersist = this.groupRepository.findByIdInAndClient(userDTO.getGroupIds(), clientEntity);
            if (groupsPersist.size() != userDTO.getGroupIds().size()) {
                throw new EntityNotFoundException(ErrorMessageConstant.GROUP_NOT_FOUND);
            }
        }
        userEntity.setGroups(groupsPersist);
    }

    private void persistRoles(UserDTO userDTO, UserEntity userEntity) {
        Set<RoleEntity> rolesPersist = new HashSet<>();
        if (!CollectionUtils.isEmpty(userDTO.getRoleIds())) {
            if(AuthConstants.VNSKY_CLIENT_ALIAS.equals(userEntity.getClient().getCode())){
                rolesPersist = this.roleRepository.findByIdInAndAppId(userDTO.getRoleIds(), applicationProperties.getVnskyWebOAuth2ClientInfo().getId());
            }else{
                List<String> appIds = List.of(applicationProperties.getPartnerWebOAuth2ClientInfo().getId(), applicationProperties.getSaleAppOAuth2ClientInfo().getId());
                rolesPersist = roleRepository.findByIdInAndAppIdIn(userDTO.getRoleIds(), appIds);
            }

            if (rolesPersist.size() != userDTO.getRoleIds().size()) {
                throw new EntityNotFoundException(ErrorMessageConstant.ROLE_NOT_FOUND);
            }
        }
        userEntity.setRoles(rolesPersist);
    }

    private void persistDepartments(UserDTO userDTO, UserEntity userEntity) {
        Set<DepartmentEntity> departmentsPersist = new HashSet<>();
        if (!CollectionUtils.isEmpty(userDTO.getDepartmentIds())) {
            departmentsPersist = new HashSet<>(this.departmentRepository.findAllById(userDTO.getDepartmentIds()));
            if (departmentsPersist.size() != userDTO.getDepartmentIds().size()) {
                throw new EntityNotFoundException(ErrorMessageConstant.DEPARTMENT_NOT_FOUND);
            }
        }
        userEntity.setDepartments(departmentsPersist);
    }

    private void checkExisted(UserDTO usersDTO) {
        if (!CollectionUtils.isEmpty(usersDTO.getGroupIds())) {
            int total = this.groupRepository.findTotalById(usersDTO.getGroupIds());
            if (usersDTO.getGroupIds().size() != total) {
                throw new EntityNotFoundException(ErrorMessageConstant.GROUP_NOT_FOUND);
            }
        }
        if (!CollectionUtils.isEmpty(usersDTO.getRoleIds())) {
            int total = this.roleRepository.findTotalById(usersDTO.getRoleIds());
            if (usersDTO.getRoleIds().size() != total) {
                throw new EntityNotFoundException(ErrorMessageConstant.ROLE_NOT_FOUND);
            }
        }
        if (!CollectionUtils.isEmpty(usersDTO.getDepartmentIds())) {
            int total = this.departmentRepository.findTotalById(usersDTO.getDepartmentIds());
            if (usersDTO.getDepartmentIds().size() != total) {
                throw new EntityNotFoundException(ErrorMessageConstant.DEPARTMENT_NOT_FOUND);
            }
        }
    }

    private boolean isUserBelongToClient(UserEntity userEntity, String clientIdentity) {
        ClientEntity clientEntity = userEntity.getClient();
        ClientEntity suppliedClientEntity = this.clientRepository.findByIdOrAlias(clientIdentity)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.CLIENT_NOT_FOUND));
        return suppliedClientEntity.equals(clientEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO detail(String clientIdentity, String userId) {
        return this.userRepository.findById(userId)
                .filter(userEntity -> this.isUserBelongToClient(userEntity, clientIdentity))
                .map(this.userMapper::convertEntity2DtoFull)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public UserDTO update(String clientIdentity, String id, UserDTO userDTO) {
        Map<String, String> fieldErrors = new HashMap<>();
        if (UserLoginMethod.LOGIN_GOOGLE.getValue() == userDTO.getLoginMethod() && !userDTO.getUsername().equalsIgnoreCase(userDTO.getEmail())) {
            fieldErrors.put(UserDTO.Fields.loginMethod, ErrorMessageConstant.INVALID_LOGIN_METHOD);
            throw new FieldsValidationException(fieldErrors);
        }
        this.checkExisted(userDTO);
        UserEntity oldUserEntity = this.userRepository.findById(id)
                .filter(userEntity -> this.isUserBelongToClient(userEntity, clientIdentity))
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.USER_NOT_FOUND));
        boolean isChangeStatus = !Objects.equals(userDTO.getStatus(), oldUserEntity.getStatus());
        boolean isInvalidatedUser = !Objects.equals(UserStatus.ACTIVE.getValue(), userDTO.getStatus());
        boolean isChangeStatusToActive = isChangeStatus && !isInvalidatedUser;
        if (isChangeStatusToActive) {
            oldUserEntity.setLoginFailedCount(0);
        }
        this.userMapper.patch(oldUserEntity, userDTO);
        oldUserEntity.setStatus(userDTO.getStatus());
        this.persistRoles(userDTO, oldUserEntity);
        this.persistGroups(userDTO, oldUserEntity);
        this.persistDepartments(userDTO, oldUserEntity);
        this.userRepository.saveAndFlush(oldUserEntity);
        this.objectService.refreshUserInfoCache(id);
        userDTO = this.userMapper.convertEntity2DtoFull(oldUserEntity);
        return userDTO;
    }

    @Override
    @Transactional
    public DeleteCountDTO delete(String clientIdentity, List<String> ids) {
        for (String id : ids) {
            UserEntity userEntity = this.userRepository.findById(id)
                    .filter(currentUserEntity -> this.isUserBelongToClient(currentUserEntity, clientIdentity))
                    .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.USER_NOT_FOUND));
            if (AuthConstants.OWNER_TYPE.equals(userEntity.getType())) {
                throw new BusinessException("error.message.cannot-delete-owner-user");
            }
            userEntity.getGroups().clear();
            userEntity.getRoles().clear();
            userEntity.getDepartments().clear();
            this.userRepository.saveAndFlush(userEntity);
            this.userRepository.delete(userEntity);
            this.userRepository.releaseFcmTokensByClientIdAndUserId(userEntity.getClient().getId(), id);
            this.objectService.refreshUserInfoCache(userEntity.getId());
        }
        return new DeleteCountDTO(ids.size());
    }

    @Override
    @SneakyThrows
    @Transactional(readOnly = true)
    public ModifyResultDTO forgotPasswordInit(ForgotPasswordInitDTO forgotPasswordInitDTO) {
        String url;
        String origin = RequestUtil.getRequestOrigin();
        forgotPasswordInitDTO.setEmail(forgotPasswordInitDTO.getEmail().toLowerCase());
        if (StringUtils.hasText(forgotPasswordInitDTO.getUsername())) {
            forgotPasswordInitDTO.setUsername(forgotPasswordInitDTO.getUsername().toLowerCase());
        }
        log.info("Init password origin: {}", origin);
        if (StringUtils.hasText(origin)) {
            url = UriComponentsBuilder.newInstance()
                    .uri(URI.create(origin))
                    .path(forgotPasswordInitDTO.getCallbackUri())
                    .build()
                    .toUriString();
        } else {
            url = forgotPasswordInitDTO.getCallbackUri();
        }
        Map<String, String> fieldErrors = new HashMap<>();
        ClientDTO clientDTO = this.clientRepository.findByIdOrAlias(forgotPasswordInitDTO.getClientIdentity())
                .map(this.clientMapper::toDto)
                .orElse(null);
        if (clientDTO == null) {
            fieldErrors.put(ForgotPasswordInitDTO.Fields.clientIdentity, ErrorMessageConstant.CLIENT_NOT_FOUND);
            throw new FieldsValidationException(fieldErrors);
        }
        String email = forgotPasswordInitDTO.getEmail();
        UserDTO userDTO = this.userRepository.findByClientIdAndEmail(clientDTO.getId(), email)
                .map(this.userMapper::toDto)
                .orElse(null);
        if (userDTO == null) {
            fieldErrors.put(ForgotPasswordInitDTO.Fields.email, ErrorMessageConstant.EMAIL_NOT_EXIST);
            throw new FieldsValidationException(fieldErrors);
        }
        String token = RandomUtil.generateNewToken();
        MailInfoDTO mailInfo = MailInfoDTO.builder()
                .to(forgotPasswordInitDTO.getEmail())
                .expireTime(AuthConstants.EmailContent.MAIL_EXPIRE_TIME)
                .subject(AuthConstants.EmailContent.MAIL_SUBJECT_FORGOT)
                .companyName(paramRepository.getCompanyName())
                .url(url + token)
                .build();
        this.mailService.sendForgotPassword(mailInfo);
        this.forgotPasswordCache.put(token, userDTO);
        return ModifyResultDTO.builder()
                .count(0)
                .message("Reset password email sent")
                .build();
    }

    @SneakyThrows
    @Override
    @Transactional
    public ModifyResultDTO forgotPasswordConfirm(ForgotPasswordConfirmDTO forgotPasswordConfirmDTO) {
        UserDTO userDTO = this.forgotPasswordCache.get(forgotPasswordConfirmDTO.getToken(), UserDTO.class);
        if (userDTO == null) {
            throw new BusinessException(ErrorMessageConstant.TOKEN_EXPIRED);
        }
        UserEntity userEntity = this.userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.USER_NOT_FOUND));
        String newPwd = StringUtil.generateRandomPassword();
        String encodedNewPwd = this.passwordEncoder.encode(newPwd);
        Integer newStatus = null;
        Integer loginFailedCount = null;
        if (UserStatus.LOCKED.getValue().equals(userEntity.getStatus())) {
            newStatus = UserStatus.ACTIVE.getValue();
            loginFailedCount = 0;
        }
        this.userRepository.updatePassword(encodedNewPwd, LocalDateTime.now(), newStatus, loginFailedCount, userEntity.getId());
        this.forgotPasswordCache.evict(forgotPasswordConfirmDTO.getToken());
        return ModifyResultDTO.builder()
                .count(1)
                .value(newPwd)
                .message("Reset password confirmed")
                .build();
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordDTO dto) {
        String userId = SecurityUtil.getCurrentUserId();
        UserEntity userEntity = this.userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.USER_NOT_FOUND));
        Map<String, String> fieldErrors = new HashMap<>();
        if (!this.passwordEncoder.matches(dto.getOldPwd(), userEntity.getPassword())) {
            fieldErrors.put(ChangePasswordDTO.Fields.oldPwd, "error.message.user.old-password-invalid");
            throw new FieldsValidationException(fieldErrors);
        }
        if (this.passwordEncoder.matches(dto.getNewPwd(), userEntity.getPassword())) {
            fieldErrors.put(ChangePasswordDTO.Fields.newPwd, "error.message.user.old-password-identical-to-new-password");
            throw new FieldsValidationException(fieldErrors);
        }
        String encodedNewPwd = this.passwordEncoder.encode(dto.getNewPwd());
        LocalDateTime passwordExp = LocalDateTime.now().plus(this.applicationProperties.getPasswordExpireTime());
        this.userRepository.updatePassword(encodedNewPwd, passwordExp, null, null, userEntity.getId());
    }

    @Override
    public List<MenuItemDTO> getMenuItemTree() {
        Optional<UserDTO> optionalUser = SecurityUtil.getCurrentUser();
        if (optionalUser.isEmpty()) {
            return new ArrayList<>();
        }
        boolean isOwner = AuthConstants.OWNER_TYPE.equals(optionalUser.get().getType());
        String appCode = SecurityUtil.getCurrentAppCode();
        List<MenuItemDTO> flatMenuList = this.objectRepository.getMenuItemHierarchy(optionalUser.get().getId(), isOwner, appCode);

        Map<String, MenuItemDTO> treeMenuMap = new HashMap<>();
        List<MenuItemDTO> treeMenuList = new ArrayList<>();

        for (MenuItemDTO menuItem : flatMenuList) {
            if (menuItem.getItems() == null) {
                menuItem.setItems(new ArrayList<>());
            }
            if (menuItem.getUri() != null) {
                menuItem.setJoinedActions(menuItem.getJoinedActions());
            }
            treeMenuMap.put(menuItem.getId(), menuItem);
            if (menuItem.getParentId() == null) {
                // root item
                treeMenuList.add(menuItem);
                continue;
            }
            MenuItemDTO parentItem = treeMenuMap.get(menuItem.getParentId());
            if (parentItem != null) {
                parentItem.getItems().add(menuItem);
            }
        }
        return this.filterTree(treeMenuList);
    }

    @Override
    public List<MenuItemDTO> getMenuItemFlat(HttpHeaders httpHeaders) {
        Optional<UserDTO> optionalUser = SecurityUtil.getCurrentUser();
        if (optionalUser.isEmpty()) {
            return new ArrayList<>();
        }
        boolean isOwner = AuthConstants.OWNER_TYPE.equals(optionalUser.get().getType());
        boolean isMobile = deviceService.getIsMobile(httpHeaders);
        log.info("[DEBUG] is mobile: {}, is owner: {}", isMobile, isOwner);
        String appCode;
        if(isMobile){
            appCode = applicationProperties.getSaleAppOAuth2ClientInfo().getClientId();
        }else{
            appCode = SecurityUtil.getCurrentAppCode();
        }
        List<MenuItemDTO> flatMenuList = this.objectRepository.getMenuItemFlat(optionalUser.get().getId(), isOwner, appCode);

        List<MenuItemDTO> filteredItems = new ArrayList<>();
        for (MenuItemDTO menuItemDTO : flatMenuList) {
            if (menuItemDTO.getUri() != null && menuItemDTO.getActions() != null && menuItemDTO.getActions().length > 0) {
                filteredItems.add(menuItemDTO);
            }
        }
        return filteredItems;
    }

    private List<MenuItemDTO> filterTree(List<MenuItemDTO> treeMenuList) {
        List<MenuItemDTO> filteredItems = new ArrayList<>();
        for (MenuItemDTO menuItemDTO : treeMenuList) {
            if (menuItemDTO.getUri() != null) {
                if (menuItemDTO.getActions() != null && menuItemDTO.getActions().length > 0) {
                    filteredItems.add(menuItemDTO);
                }
            } else {
                List<MenuItemDTO> filteredChildItems = this.filterTree(menuItemDTO.getItems());
                menuItemDTO.setItems(filteredChildItems);
                if (!filteredChildItems.isEmpty()) {
                    filteredItems.add(menuItemDTO);
                }
            }
        }
        return filteredItems;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getProfile() {
        UserDTO userDTO = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new PreAuthenticatedCredentialsNotFoundException(ErrorMessageConstant.SYSTEM_ERROR));
        UserDTO userProfileDTO = this.userRepository.findById(userDTO.getId())
                .map(this.userMapper::convertEntity2DtoFull)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.USER_NOT_FOUND));
        return this.checkPasswordChangeRequirement(userDTO, userProfileDTO);
    }

    @Override
    @Transactional
    public UserDTO updateProfile(UserDTO userDTO) {
        UserEntity userEntity = this.userRepository
                .findById(SecurityUtil.getCurrentUserId())
                .orElseThrow(() -> new PreAuthenticatedCredentialsNotFoundException(ErrorMessageConstant.SYSTEM_ERROR));
        if(Objects.equals(SecurityUtil.getCurrentClientId(), AuthConstants.VNSKY_CLIENT_ID)){
            this.userMapper.patch(userEntity, userDTO);
        }else{
            userEntity.setFullname(userDTO.getFullname());
        }
        this.persistDepartments(userDTO, userEntity);
        this.userRepository.saveAndFlush(userEntity);
        UserDTO userProfileDTO = this.userMapper.convertEntity2DtoFull(userEntity);
        return this.checkPasswordChangeRequirement(userDTO, userProfileDTO);

    }

    @Override
    @Transactional
    public UserDTO updateProfile(UserDTO userDTO, MultipartFile signatureImageFile) {
        // Update profile information
        UserDTO updatedProfile = this.updateProfile(userDTO);
        
        // Upload signature image if provided
        if (signatureImageFile != null && !signatureImageFile.isEmpty()) {
            this.updateSignature(signatureImageFile);
        }
        
        return updatedProfile;
    }

    @NotNull
    private UserDTO checkPasswordChangeRequirement(UserDTO userDTO, UserDTO userProfileDTO) {
        boolean needChangePassword;
        if (userDTO.getAttribute(AppDTO.Fields.ssoProvider) != null) {
            // login via SSO
            needChangePassword = false;
        } else {
            needChangePassword = userProfileDTO.getPasswordExpireTime() != null &&
                                 userProfileDTO.getPasswordExpireTime().isBefore(LocalDateTime.now());
        }
        userProfileDTO.setNeedChangePassword(needChangePassword);
        return userProfileDTO;
    }

    @Override
    @Transactional
    public ModifyResultDTO initFcm(String token) {
        String clientId = SecurityUtil.getCurrentClientId();
        String userId = SecurityUtil.getCurrentUserId();
        Optional<FcmTokenEntity> fcmTokenEntityOptional = this.fcmUserTokenRepository.findById(token);
        FcmTokenEntity fcmTokenEntity;
        if (fcmTokenEntityOptional.isPresent()) {
            fcmTokenEntity = fcmTokenEntityOptional.get();
            if (!fcmTokenEntity.getClientId().equals(clientId) || !fcmTokenEntity.getUserId().equals(userId)) {
                fcmTokenEntity.setClientId(clientId);
                fcmTokenEntity.setUserId(userId);
                this.fcmUserTokenRepository.saveAndFlush(fcmTokenEntity);
                return ModifyResultDTO.builder()
                        .count(1)
                        .build();
            } else {
                fcmTokenEntity.setLastAccessTime(LocalDateTime.now());
            }
        } else {
            fcmTokenEntity = FcmTokenEntity.builder()
                    .id(token)
                    .userId(userId)
                    .clientId(clientId)
                    .createdTime(LocalDateTime.now())
                    .build();
            this.fcmUserTokenRepository.saveAndFlush(fcmTokenEntity);
        }
        return ModifyResultDTO.builder()
                .count(1)
                .build();
    }

    @Override
    public List<FcmTokenDTO> getFcmTokens() {
        String clientId = SecurityUtil.getCurrentClientId();
        String userId = SecurityUtil.getCurrentUserId();
        return this.fcmUserTokenRepository.findByClientIdAndUserId(clientId, userId)
                .stream()
                .map(e -> FcmTokenDTO.builder()
                        .token(e.getId())
                        .createdTime(e.getLastAccessTime())
                        .lastAccessTime(e.getLastAccessTime())
                        .build())
                .toList();
    }

    private String getCurrentUserSignaturePath() {
        String userId = SecurityUtil.getCurrentUserId();
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .pathSegment(USER_SIGNATURE_PATH, userId + AuthConstants.CommonSymbol.DOT + USER_SIGNATURE_EXT)
                .build();
        return uriComponents.toUriString();
    }

    @SneakyThrows
    @Override
    public Pair<String, Resource> getSignature() {
        String signaturePath = this.getCurrentUserSignaturePath();
        String etag;
        Resource resource;
        try {
            etag = SecurityUtil.getCurrentUserId();
            resource = this.storageService.download(signaturePath);
        } catch (Exception e) {
            BufferedImage bi = new BufferedImage(412, 180, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bi.createGraphics();
            // To be sure, we use clearRect, which will (unlike fillRect) totally replace
            // the current pixels with the desired color, even if it's fully transparent.
            g2d.setComposite(AlphaComposite.Clear);
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillRect(0, 0, 10, 10);
            g2d.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            byte[] bytes = baos.toByteArray();
            etag = "";
            resource = new ByteArrayResource(bytes);
        }
        return Pair.of(etag, resource);
    }

    @SneakyThrows
    public void updateSignature(MultipartFile signatureImageFile) {
        try (InputStream imgIS1 = signatureImageFile.getInputStream();
             InputStream imgIS2 = signatureImageFile.getInputStream()) {
            ImageInputStream iis = ImageIO.createImageInputStream(imgIS1);
            Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
            BufferedImage bufferedImage = null;
            if (imageReaders.hasNext()) {
                ImageReader reader = imageReaders.next();
                reader.setInput(iis);
                log.info("formatName: {}", reader.getFormatName());
                if (!USER_SIGNATURE_EXT.equals(reader.getFormatName())) {
                    throw new BusinessException("error.message.image.png-format-only");
                }
                if (reader.getWidth(0) > USER_SIGNATURE_MAX_WIDTH || reader.getHeight(0) > USER_SIGNATURE_MAX_HEIGHT) {
                    throw new BusinessException("error.message.image.size-too-large");
                }
                if (signatureImageFile.getSize() > DataSize.ofMegabytes(1).toBytes()) {
                    throw new BusinessException("error.message.image.file-too-large");
                }
                bufferedImage = reader.read(0);
            }
            if (bufferedImage == null) {
                throw new BusinessException("error.message.image.invalid");
            }
            String signaturePath = this.getCurrentUserSignaturePath();
            this.storageService.upload(imgIS2, signaturePath);
        } catch (IOException ex) {
            log.error("Error while reading image file", ex);
            throw new BusinessException("error.message.image.invalid");
        }
    }

    @Override
    public void deleteSignature() {
        String signaturePath = this.getCurrentUserSignaturePath();
        this.storageService.delete(signaturePath);
    }

    @Async
    @Override
    public void sendAccessLog(RegisteredClient registeredClient,
                              Authentication authentication,
                              String actionType, String clientIp) {
        AccessLogDTO accessLogDTO = AccessLogDTO.builder()
                .accessTime(ZonedDateTime.now())
                .actionType(actionType)
                .clientIp(clientIp)
                .status(AuthConstants.ModelStatus.ACTIVE)
                .build();
        Map<String, Object> authHeaders = new HashMap<>();
        if (authentication.getPrincipal() instanceof UserDTO userDTO) {
            authHeaders.put("CLIENT_ID", userDTO.getClient().getId());
            authHeaders.put("CLIENT_CODE", userDTO.getClient().getCode());
            authHeaders.put("CLIENT_NAME", userDTO.getClient().getName());
            if (authentication.getDetails() instanceof AppDTO appDTO) {
                authHeaders.put("SITE_ID", appDTO.getId());
                authHeaders.put("SITE_CODE", appDTO.getCode());
                authHeaders.put("SITE_NAME", appDTO.getName());
                if (Objects.nonNull(appDTO.getSsoProvider())) {
                    authHeaders.put("SSO_PROVIDER", appDTO.getSsoProvider());
                }
            }
            authHeaders.put("USER_ID", userDTO.getId());
            authHeaders.put("USER_USERNAME", userDTO.getUsername());
            authHeaders.put("USER_FULLNAME", userDTO.getFullname());
            authHeaders.put("USER_PREFERRED_USERNAME", SecurityUtil.getPreferredUsername(userDTO));
        } else {
            log.error("UserDTO not found in current security context");
            return;
        }
        String msgKey = UUID.randomUUID().toString();
        List<Header> headers = authHeaders.entrySet()
                .stream()
                .map(e -> {
                    if (e.getValue() instanceof String strVal) {
                        return (Header) new RecordHeader(e.getKey(), strVal.getBytes(StandardCharsets.UTF_8));
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(ACCESS_LOG_QUEUE, null, System.currentTimeMillis(), msgKey, accessLogDTO, headers);
        this.kafkaOperations.send(producerRecord).handleAsync((result, throwable) -> {
            if (throwable != null) {
                log.error("[SYSTEM_ACCESS_LOG] Send message to topic {} failed, error: ", ACCESS_LOG_QUEUE, throwable);
            } else if (log.isDebugEnabled()) {
                log.info("[SYSTEM_ACCESS_LOG] Send message to topic {} successfully", ACCESS_LOG_QUEUE);
            }
            return null;
        });
    }

    @Override
    public Page<UserDTO> searchPartnerUsers(String term, Integer status, Pageable pageable) {
        term = StringUtil.buildLikeOperator(term);
        return userRepository.findPartnerUsers(VNSKY_CLIENT_ID, term, status, pageable).map(this.userMapper::toDto);
    }

    @Override
    public List<String> searchUserByObjectCode(List<String> objectCodes) {
        return userRepository.findByObjectCode(objectCodes);
    }
}
