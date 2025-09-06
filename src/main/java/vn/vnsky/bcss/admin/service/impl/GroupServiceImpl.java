package vn.vnsky.bcss.admin.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.constant.ErrorMessageConstant;
import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.GroupDTO;
import vn.vnsky.bcss.admin.entity.ClientEntity;
import vn.vnsky.bcss.admin.entity.GroupEntity;
import vn.vnsky.bcss.admin.entity.RoleEntity;
import vn.vnsky.bcss.admin.entity.UserEntity;
import vn.vnsky.bcss.admin.error.FieldsValidationException;
import vn.vnsky.bcss.admin.mapper.GroupMapper;
import vn.vnsky.bcss.admin.repository.ClientRepository;
import vn.vnsky.bcss.admin.repository.GroupRepository;
import vn.vnsky.bcss.admin.repository.RoleRepository;
import vn.vnsky.bcss.admin.repository.UserRepository;
import vn.vnsky.bcss.admin.service.GroupService;
import vn.vnsky.bcss.admin.service.ObjectService;
import vn.vnsky.bcss.admin.util.StringUtil;

import java.util.*;

/**
 * Service Implementation for managing {@link GroupEntity}.
 */
@Slf4j
@Service
public class GroupServiceImpl implements GroupService {

    private final ApplicationProperties applicationProperties;

    private final ClientRepository clientRepository;

    private final GroupRepository groupRepository;

    private final RoleRepository roleRepository;

    private final UserRepository usersRepository;

    private final GroupMapper groupMapper;

    private final ObjectService objectService;

    @Autowired
    public GroupServiceImpl(ApplicationProperties applicationProperties,
                            ClientRepository clientRepository, GroupRepository groupRepository,
                            RoleRepository roleRepository, UserRepository usersRepository,
                            GroupMapper groupMapper, ObjectService objectService) {
        this.applicationProperties = applicationProperties;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.roleRepository = roleRepository;
        this.usersRepository = usersRepository;
        this.groupMapper = groupMapper;
        this.objectService = objectService;
    }

    private String determineAppCode(String clientIdentity) {
        return AuthConstants.VNSKY_CLIENT_ALIAS.equals(clientIdentity) ?
                this.applicationProperties.getVnskyWebOAuth2ClientInfo().getClientId() :
                this.applicationProperties.getPartnerWebOAuth2ClientInfo().getClientId()
                ;
    }

    /**
     * Search groups.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<GroupDTO> search(String clientIdentity, String term, Integer status, Pageable pageable) {
        String fmtTerm = StringUtil.buildLikeOperatorLower(term);
        ClientEntity clientEntity = this.clientRepository.findByIdOrAlias(clientIdentity)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.CLIENT_NOT_FOUND));
        return this.groupRepository.findByTerm(clientEntity.getId(), fmtTerm, status, pageable)
                .map(groupMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupDTO> all(String clientIdentity) {
        ClientEntity clientEntity = this.clientRepository.findByIdOrAlias(clientIdentity)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.CLIENT_NOT_FOUND));
        return this.groupRepository.findAllByClient(clientEntity)
                .stream()
                .map(groupMapper::mapperToDTOIgnoring)
                .toList();
    }

    /**
     * Get one groups by id.
     *
     * @param clientIdentity
     * @param id             the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public GroupDTO detail(String clientIdentity, String id) {
        log.debug("Request to get Groups : {}", id);
        return this.groupRepository.findById(id)
                .filter(groupEntity -> this.isGroupBelongToClient(groupEntity, clientIdentity))
                .map(groupMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("error.message.group.id-not-existed"));
    }

    /**
     * create a groups.
     *
     * @param clientIdentity
     * @param groupDTO       the entity to save.
     * @return the persisted entity.
     */
    @Override
    @Transactional
    public GroupDTO create(String clientIdentity, GroupDTO groupDTO) {
        ClientEntity clientEntity = this.clientRepository.findByIdOrAlias(clientIdentity)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.CLIENT_NOT_FOUND));
        log.debug("Request to create Group : {}", groupDTO);
        if (this.groupRepository.existsByClientAndCode(clientEntity.getId(), groupDTO.getCode(), null)) {
            throw new FieldsValidationException(Collections.singletonMap("code", "error.message.group.code-existed"));
        }
        GroupEntity groupEntity = this.groupMapper.toEntity(groupDTO);
        this.persistenceRole(groupDTO, groupEntity);
        this.persistenceUser(groupDTO, groupEntity);
        groupEntity.setCode(groupDTO.getCode().toUpperCase());
        groupEntity.setClient(clientEntity);
        groupEntity = this.groupRepository.saveAndFlush(groupEntity);
        Set<String> affectedUserIds = this.groupRepository.findAllAffectedUserIds(groupEntity.getId());
        this.objectService.clearUserPolicyCache(affectedUserIds, this.determineAppCode(clientIdentity));
        return this.groupMapper.toDto(groupEntity);
    }

    private boolean isGroupBelongToClient(GroupEntity groupEntity, String clientIdentity) {
        ClientEntity clientEntity = groupEntity.getClient();
        ClientEntity suppliedClientEntity = this.clientRepository.findByIdOrAlias(clientIdentity)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.CLIENT_NOT_FOUND));
        return suppliedClientEntity.equals(clientEntity);
    }

    /**
     * update a groups.
     *
     * @param clientIdentity
     * @param id             id of group
     * @param groupDTO       the entity to save.
     * @return the persisted entity.
     */
    @Override
    @Transactional
    public GroupDTO update(String clientIdentity, String id, GroupDTO groupDTO) {
        log.debug("Request to update Group : {}", groupDTO);
        GroupEntity oldGroupEntity = this.groupRepository.findById(id)
                .filter(groupEntity -> this.isGroupBelongToClient(groupEntity, clientIdentity))
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.GROUP_NOT_FOUND));
        String clientId = oldGroupEntity.getClient().getId();
        if (this.groupRepository.existsByClientAndCode(clientId, groupDTO.getCode(), groupDTO.getId())) {
            throw new FieldsValidationException(Collections.singletonMap("code", "error.message.group.code-existed"));
        }
        Set<String> affectedUserIds = this.groupRepository.findAllAffectedUserIds(oldGroupEntity.getId());
        this.groupMapper.patchEntity(oldGroupEntity, groupDTO);
        this.persistenceRole(groupDTO, oldGroupEntity);
        this.persistenceUser(groupDTO, oldGroupEntity);
        oldGroupEntity.setCode(groupDTO.getCode().toUpperCase());
        oldGroupEntity = this.groupRepository.saveAndFlush(oldGroupEntity);
        affectedUserIds.addAll(this.groupRepository.findAllAffectedUserIds(oldGroupEntity.getId()));
        this.objectService.clearUserPolicyCache(affectedUserIds, this.determineAppCode(clientIdentity));
        return this.groupMapper.toDto(oldGroupEntity);
    }

    private void persistenceRole(GroupDTO groupsDTO, GroupEntity groupEntity) {
        Set<RoleEntity> rolesPersist = new HashSet<>();
        if (!CollectionUtils.isEmpty(groupsDTO.getRoleIds())) {
            groupsDTO.getRoleIds().stream().filter(Objects::nonNull).forEach(id -> {
                Optional<RoleEntity> role = this.roleRepository.findById(id);
                if (role.isPresent()) {
                    rolesPersist.add(role.get());
                } else {
                    throw new EntityNotFoundException(ErrorMessageConstant.ROLE_NOT_FOUND);
                }
            });
        }
        groupEntity.setRoles(rolesPersist);
    }

    private void persistenceUser(GroupDTO groupsDTO, GroupEntity groupEntity) {
        Set<UserEntity> usersPersist = new HashSet<>();
        if (!CollectionUtils.isEmpty(groupsDTO.getUserIds())) {
            groupsDTO.getUserIds().stream().filter(Objects::nonNull).forEach(id -> {
                Optional<UserEntity> users = this.usersRepository.findById(id);
                if (users.isPresent()) {
                    usersPersist.add(users.get());
                } else {
                    throw new EntityNotFoundException(ErrorMessageConstant.USER_NOT_FOUND);
                }
            });
        }
        groupEntity.setUsers(usersPersist);
    }

    @Override
    @Transactional
    public DeleteCountDTO delete(String clientIdentity, List<String> ids) {
        log.debug("Request to delete Groups : {}", ids);
        for (String id : ids) {
            Set<String> affectedUserIds = this.groupRepository.findAllAffectedUserIds(id);
            GroupEntity oldGroupEntity = this.groupRepository.findById(id)
                    .filter(groupEntity -> this.isGroupBelongToClient(groupEntity, clientIdentity))
                    .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.GROUP_NOT_FOUND));
            oldGroupEntity.getRoles().clear();
            oldGroupEntity.getUsers().clear();
            this.groupRepository.saveAndFlush(oldGroupEntity);
            this.groupRepository.delete(oldGroupEntity);
            this.objectService.clearUserPolicyCache(affectedUserIds, this.determineAppCode(clientIdentity));
        }
        return new DeleteCountDTO(ids.size());
    }

}
