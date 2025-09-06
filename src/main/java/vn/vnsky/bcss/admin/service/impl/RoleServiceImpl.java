package vn.vnsky.bcss.admin.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.constant.ErrorMessageConstant;
import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.RoleDTO;
import vn.vnsky.bcss.admin.entity.RoleEntity;
import vn.vnsky.bcss.admin.error.FieldsValidationException;
import vn.vnsky.bcss.admin.mapper.RoleMapper;
import vn.vnsky.bcss.admin.repository.ObjectRepository;
import vn.vnsky.bcss.admin.repository.RoleRepository;
import vn.vnsky.bcss.admin.service.DeviceService;
import vn.vnsky.bcss.admin.service.ObjectService;
import vn.vnsky.bcss.admin.service.RoleService;
import vn.vnsky.bcss.admin.util.SecurityUtil;
import vn.vnsky.bcss.admin.util.StringUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final ApplicationProperties applicationProperties;

    private final ObjectService objectService;

    private final RoleRepository roleRepository;

    private final ObjectRepository objectRepository;

    private final RoleMapper roleMapper;

    private final DeviceService deviceService;

    @Autowired
    public RoleServiceImpl(ApplicationProperties applicationProperties, ObjectService objectService, RoleRepository roleRepository,
                           ObjectRepository objectRepository, RoleMapper roleMapper,
                           DeviceService deviceService) {
        this.applicationProperties = applicationProperties;
        this.objectService = objectService;
        this.roleRepository = roleRepository;
        this.objectRepository = objectRepository;
        this.roleMapper = roleMapper;
        this.deviceService = deviceService;
    }

    private String determineAppCode(boolean isPartner) {
        return isPartner ?
                this.applicationProperties.getPartnerWebOAuth2ClientInfo().getClientId() :
                this.applicationProperties.getVnskyWebOAuth2ClientInfo().getClientId()
                ;
    }

    @Transactional(readOnly = true)
    public Page<RoleDTO> search(boolean isPartner, String term, Integer status, Pageable pageable) {
        log.debug("Request to get list Roles");
        String fmtTerm = StringUtil.buildLikeOperatorLower(term);
        if(!isPartner) {
            String appId = this.applicationProperties.getVnskyWebOAuth2ClientInfo().getId();
            return this.roleRepository.findByTerm(fmtTerm, appId, status, pageable)
                    .map(roleMapper::toDto);
        }

        List<String> appIds = List.of(applicationProperties.getPartnerWebOAuth2ClientInfo().getId(),
                                        applicationProperties.getSaleAppOAuth2ClientInfo().getId());
        return this.roleRepository.findByTermIn(fmtTerm, appIds, status, pageable)
                .map(roleMapper::toDto);
    }

    @Override
    public List<RoleDTO> all(boolean isPartner) {
        if(!isPartner) {
            return this.roleRepository.findAllByAppId(applicationProperties.getVnskyWebOAuth2ClientInfo().getId())
                    .stream()
                    .map(this.roleMapper::toDtoIgnore)
                    .toList();
        }

        List<String> appIds = List.of(applicationProperties.getPartnerWebOAuth2ClientInfo().getId(),
                        applicationProperties.getSaleAppOAuth2ClientInfo().getId());
        return this.roleRepository.findAllByAppIdIn(appIds)
                .stream()
                .map(this.roleMapper::toDtoIgnore)
                .toList();
    }

    @Transactional
    @Override
    public RoleDTO create(boolean isPartner, Boolean isMobile, RoleDTO roleDTO) {
        log.debug("Request to save Role : {}", roleDTO);
        String appId = deviceService.getWebClientInfo(isPartner, isMobile).getId();
        this.validateRole(null, appId, roleDTO);
        RoleEntity roleEntity = this.roleMapper.toEntity(roleDTO);
        roleEntity.setAppId(appId);
        roleEntity.setStatus(AuthConstants.ModelStatus.ACTIVE);
        this.roleRepository.saveAndFlush(roleEntity);
        this.updateRoleObjectAction(roleEntity.getId(), roleDTO);
        this.roleRepository.flush();
        Set<String> affectedUserIds = this.roleRepository.findAllAffectedUserIds(roleEntity.getId());
        this.objectService.clearUserPolicyCache(affectedUserIds, this.determineAppCode(isPartner));
        roleDTO = roleMapper.toDto(roleEntity);
        List<String> roleObjectIds = this.objectRepository.getRoleObjectKeys(roleEntity.getId(), AuthConstants.CommonSymbol.DASH);
        roleDTO.setCheckedKeys(roleObjectIds);
        return roleDTO;
    }

    @Override
    @Transactional
    public RoleDTO update(boolean isPartner, Boolean isMobile, String id, RoleDTO roleDTO) {
        log.debug("Request to update Role : {}", roleDTO);
        roleDTO.setId(id);
        RoleEntity roleEntity;
        if(isPartner) {
            List<String> appIds = List.of(applicationProperties.getPartnerWebOAuth2ClientInfo().getId(), applicationProperties.getSaleAppOAuth2ClientInfo().getId());
            roleEntity = this.roleRepository.findByIdAndAppIdIn(id, appIds).orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.ROLE_NOT_FOUND));
        }else{
            roleEntity = this.roleRepository.findByIdAndAppId(id, applicationProperties.getVnskyWebOAuth2ClientInfo().getId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.ROLE_NOT_FOUND));
        }

        Set<String> affectedUserIds = this.roleRepository.findAllAffectedUserIds(roleEntity.getId());
        this.validateRole(id, roleEntity.getAppId(), roleDTO);
        this.roleMapper.patch(roleEntity, roleDTO);
        roleEntity.setLastModifiedBy(SecurityUtil.getCurrentUserName());
        roleEntity.setLastModifiedDate(LocalDateTime.now());
        roleEntity = this.roleRepository.saveAndFlush(roleEntity);
        this.updateRoleObjectAction(roleEntity.getId(), roleDTO);
        this.roleRepository.flush();
        affectedUserIds.addAll(this.roleRepository.findAllAffectedUserIds(roleEntity.getId()));
        this.objectService.clearUserPolicyCache(affectedUserIds, this.determineAppCode(isPartner));
        roleDTO = roleMapper.toDto(roleEntity);
        List<String> roleObjectIds = this.objectRepository.getRoleObjectKeys(id, AuthConstants.CommonSymbol.DASH);
        roleDTO.setCheckedKeys(roleObjectIds);
        return roleDTO;
    }

    private void updateRoleObjectAction(String id, RoleDTO dto) {
        int count = this.objectRepository.deleteRoleObjectAction(dto.getId());
        Set<String> checkedKeys = new HashSet<>();
        log.info("Delete {} role_object_action", count);
        for (String checkedKey : dto.getCheckedKeys()) {
            String[] objectActionId = checkedKey.split(AuthConstants.CommonSymbol.DASH);
            if (objectActionId.length != 2 || checkedKeys.contains(checkedKey)){
                continue;
            }

            checkedKeys.add(checkedKey);
            String objectId = objectActionId[0];
            String actionId = objectActionId[1];
            this.objectRepository.insertRoleObjectAction(id, objectId, actionId);
        }
    }

    @Transactional(readOnly = true)
    public RoleDTO detail(boolean isPartner, String id) {
        log.debug("Request to get Role : {}", id);
        if (!isPartner) {
            return this.roleRepository.findByIdAndAppId(id, applicationProperties.getVnskyWebOAuth2ClientInfo().getId())
                    .map(roleMapper::toDto)
                    .map(role -> {
                        List<String> roleObjectIds = this.objectRepository.getRoleObjectKeys(id, AuthConstants.CommonSymbol.DASH);
                        role.setCheckedKeys(roleObjectIds);
                        return role;
                    })
                    .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.ROLE_NOT_FOUND));
        }
        List<String> appIds = List.of(applicationProperties.getPartnerWebOAuth2ClientInfo().getId(), applicationProperties.getSaleAppOAuth2ClientInfo().getId());
        return this.roleRepository.findByIdAndAppIdIn(id, appIds)
                .map(roleMapper::toDto)
                .map(role -> {
                    List<String> roleObjectIds = this.objectRepository.getRoleObjectKeys(id, AuthConstants.CommonSymbol.DASH);
                    role.setCheckedKeys(roleObjectIds);
                    return role;
                })
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.ROLE_NOT_FOUND));
    }

    @Override
    @Transactional
    public DeleteCountDTO delete(boolean isPartner, List<String> ids) {
        log.debug("Delete Roles: {}", ids);
        if(!isPartner){
            for (String id : ids) {
                RoleEntity roleEntity = this.roleRepository.findByIdAndAppId(id, applicationProperties.getVnskyWebOAuth2ClientInfo().getId())
                        .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.ROLE_NOT_FOUND));
                Set<String> affectedUserIds = this.roleRepository.findAllAffectedUserIds(roleEntity.getId());
                this.roleRepository.deleteUserRole(id);
                this.roleRepository.deleteGroupRole(id);
                this.objectRepository.deleteRoleObjectAction(id);
                this.roleRepository.delete(roleEntity);
                this.objectService.clearUserPolicyCache(affectedUserIds, this.determineAppCode(isPartner));
            }
            return new DeleteCountDTO(ids.size());
        }

        List<String> appIds = List.of(applicationProperties.getPartnerWebOAuth2ClientInfo().getId(), applicationProperties.getSaleAppOAuth2ClientInfo().getId());
        for (String id : ids) {
            RoleEntity roleEntity = this.roleRepository.findByIdAndAppIdIn(id, appIds)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.ROLE_NOT_FOUND));
            Set<String> affectedUserIds = this.roleRepository.findAllAffectedUserIds(roleEntity.getId());
            this.roleRepository.deleteUserRole(id);
            this.roleRepository.deleteGroupRole(id);
            this.objectRepository.deleteRoleObjectAction(id);
            this.roleRepository.delete(roleEntity);
            this.objectService.clearUserPolicyCache(affectedUserIds, this.determineAppCode(isPartner));
        }
        return new DeleteCountDTO(ids.size());
    }

    private void validateRole(String id, String appId, RoleDTO dto) {
        boolean valid;
        if (id == null) {
            valid = !this.roleRepository.existsByRoleCodeCreate(appId, dto.getCode());
        } else {
            valid = !this.roleRepository.existsByRoleCodeUpdate(dto.getId(), appId, dto.getCode());
        }
        if (!valid) {
            throw new FieldsValidationException(Collections.singletonMap("code", "error.message.role.code-existed"));
        }
    }
}
