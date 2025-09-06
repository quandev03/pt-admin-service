package vn.vnsky.bcss.admin.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.constant.ClientType;
import vn.vnsky.bcss.admin.constant.ErrorMessageConstant;
import vn.vnsky.bcss.admin.dto.ChangeStatusDTO;
import vn.vnsky.bcss.admin.dto.ClientDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;
import vn.vnsky.bcss.admin.entity.ClientEntity;
import vn.vnsky.bcss.admin.entity.UserEntity;
import vn.vnsky.bcss.admin.error.FieldsValidationException;
import vn.vnsky.bcss.admin.mapper.ClientMapper;
import vn.vnsky.bcss.admin.mapper.UserMapper;
import vn.vnsky.bcss.admin.repository.ClientRepository;
import vn.vnsky.bcss.admin.repository.UserRepository;
import vn.vnsky.bcss.admin.service.ClientService;
import vn.vnsky.bcss.admin.util.StringUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ClientServiceImpl implements ClientService {

    private static final String USERNAME_NOT_EMPTY = "error.message.user.username-not-empty";

    private static final String PASSWORD_NOT_EMPTY = "error.message.user.password-not-empty";

    private static final String CODE_EXIST = "error.message.client.alias-exist";

    private final ApplicationProperties applicationProperties;

    private final PasswordEncoder passwordEncoder;

    private final ClientRepository clientRepository;

    private final UserRepository userRepository;

    private final ClientMapper clientMapper;

    private final UserMapper userMapper;

    @Autowired
    public ClientServiceImpl(ApplicationProperties applicationProperties, PasswordEncoder passwordEncoder,
                             ClientRepository clientRepository, UserRepository userRepository,
                             ClientMapper clientMapper, UserMapper userMapper) {
        this.applicationProperties = applicationProperties;
        this.passwordEncoder = passwordEncoder;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.clientMapper = clientMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ClientDTO loadClientByIdentityOrAlias(String clientIdentity, ClientType clientType) {
        return this.clientRepository.findByIdOrAlias(clientIdentity)
                .filter(clientEntity -> clientType.getIdPattern().matcher(clientEntity.getId()).matches())
                .map(this.clientMapper::toDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientDTO> search(String term, Pageable pageable, ClientType clientType) {
        String fmtTerm = StringUtil.buildLikeOperator(term);
        return this.clientRepository.findByTerm(fmtTerm, clientType.getValue(), pageable)
                .map(this.clientMapper::toDto)
                ;
    }

    @Override
    @Transactional
    public ClientDTO create(ClientDTO clientDTO, ClientType clientType) {
        Map<String, String> fieldErrors = new HashMap<>();
        ClientEntity clientEntity = this.clientMapper.toEntity(clientDTO);
        List<UserDTO> users = clientDTO.getUsers();
        if (!CollectionUtils.isEmpty(users)) {
            UserDTO userDTO = users.get(0);
            String username = userDTO.getUsername();
            String password = userDTO.getPassword();
            if (!StringUtils.hasText(username)) fieldErrors.put("username", USERNAME_NOT_EMPTY);
            if (!StringUtils.hasText(password)) fieldErrors.put("password", PASSWORD_NOT_EMPTY);
            UserEntity userEntity = userMapper.toEntity(userDTO);
            userEntity.setPassword(this.passwordEncoder.encode(userEntity.getPassword()));
            userEntity.setClient(clientEntity);
            userEntity.setType(AuthConstants.OWNER_TYPE);
            userEntity.setGender(1);
            userEntity.setLoginFailedCount(0);
            userEntity.setPasswordExpireTime(LocalDateTime.now().plus(this.applicationProperties.getPasswordExpireTime()));
            userEntity.setStatus(AuthConstants.ModelStatus.ACTIVE);
            this.userRepository.saveAndFlush(userEntity);
        }

        String clientAlias = clientDTO.getCode();
        if (StringUtils.hasText(clientAlias)) {
            boolean clientExist = this.clientRepository.existsByCodeOrId(clientAlias, clientAlias);
            if (clientExist) {
                fieldErrors.put("code", CODE_EXIST);
            }
        }
        if (!ObjectUtils.isEmpty(fieldErrors)) throw new FieldsValidationException(fieldErrors);
        Long clientOrdinal = this.clientRepository.nextClientSeq();
        String clientId = String.format("%012d", clientOrdinal);
        clientEntity.setId(clientId);
        clientEntity.setStatus(AuthConstants.ModelStatus.ACTIVE);
        clientEntity = this.clientRepository.saveAndFlush(clientEntity);
        this.clientRepository.saveAndFlush(clientEntity);
        return this.clientMapper.toDto(clientEntity);
    }

    @Override
    @Transactional
    public ClientDTO patch(String identity, ClientDTO clientDTO, ClientType clientType) {
        ClientEntity clientEntity = this.clientRepository.findByIdOrAlias(identity)
                .filter(e -> clientType.getIdPattern().matcher(e.getId()).matches())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.CLIENT_NOT_FOUND));
        this.clientMapper.patch(clientEntity, clientDTO);
        return this.clientMapper.toDto(clientEntity);
    }

    @Override
    @Transactional
    public ClientDTO changeStatus(String identity, ChangeStatusDTO clientDTO, ClientType clientType) {
        ClientEntity clientEntity = this.clientRepository.findByIdOrAlias(identity)
                .filter(e -> clientType.getIdPattern().matcher(e.getId()).matches())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.CLIENT_NOT_FOUND));
        clientEntity.setStatus(clientDTO.getStatus());
        return this.clientMapper.toDto(clientEntity);
    }

    @Override
    @Transactional
    public ClientDTO detail(String identity, ClientType clientType) {
        return this.clientRepository.findByIdOrAlias(identity)
                .filter(e -> clientType.getIdPattern().matcher(e.getId()).matches())
                .map(this.clientMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessageConstant.CLIENT_NOT_FOUND));
    }

}
