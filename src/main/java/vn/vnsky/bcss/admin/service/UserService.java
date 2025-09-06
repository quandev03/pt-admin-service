package vn.vnsky.bcss.admin.service;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.web.multipart.MultipartFile;
import vn.vnsky.bcss.admin.dto.*;

import java.util.List;

public interface UserService {

    UserDTO loadUserByClientAndUser(String clientId, String username);

    boolean lockUserForFailedLoginAttempts(UserDTO userDTO);

    void resetUserFailedLoginAttempts(UserDTO userDTO);

    Page<UserDTO> search(String clientIdentity, String term, Integer status, Pageable pageable);

    List<UserDTO> all(String clientIdentity, List<String> permissions, String departmentCode, Boolean isActive, String sort);

    List<UserDTO> all(Boolean isPartner, String term);

    UserDTO create(String clientIdentity, UserDTO userDTO);

    UserDTO createPartner( String orgId, UserDTO userDTO);

    UserDTO detail(String clientIdentity, String userId);

    UserDTO update(String clientIdentity, String id, UserDTO userDTO);

    DeleteCountDTO delete(String clientIdentity, List<String> ids);

    ModifyResultDTO forgotPasswordInit(ForgotPasswordInitDTO forgotPasswordInitDTO);

    ModifyResultDTO forgotPasswordConfirm(ForgotPasswordConfirmDTO forgotPasswordConfirmDTO);

    void changePassword(ChangePasswordDTO dto);

    List<MenuItemDTO> getMenuItemTree();

    List<MenuItemDTO> getMenuItemFlat(HttpHeaders headers);

    UserDTO getProfile();

    UserDTO updateProfile(UserDTO userDTO);

    ModifyResultDTO initFcm(String token);

    List<FcmTokenDTO> getFcmTokens();

    Pair<String, Resource> getSignature();

    void updateSignature(@Valid MultipartFile signatureImageFile);

    void deleteSignature();

    void sendAccessLog(RegisteredClient registeredClient,
                       Authentication authentication,
                       String actionType, String clientIp);
    Page<UserDTO> searchPartnerUsers(String term, Integer status, Pageable pageable);

    List<String> searchUserByObjectCode(List<String> objectCodes);
}
