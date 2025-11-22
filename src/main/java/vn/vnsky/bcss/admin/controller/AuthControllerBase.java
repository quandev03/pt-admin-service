package vn.vnsky.bcss.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.vnsky.bcss.admin.dto.*;
import vn.vnsky.bcss.admin.service.DeviceService;
import vn.vnsky.bcss.admin.service.ObjectService;
import vn.vnsky.bcss.admin.service.UserService;
import vn.vnsky.bcss.admin.util.SecurityUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public abstract class AuthControllerBase {

    protected final UserService userService;

    private final ObjectService objectService;

    private final DeviceService deviceService;

    protected AuthControllerBase(UserService userService, ObjectService objectService, DeviceService deviceService) {
        this.userService = userService;
        this.objectService = objectService;
        this.deviceService = deviceService;
    }

    @Operation(summary = "api lấy cây chức năng")
    @SneakyThrows
    @GetMapping("/menu/tree")
    public ResponseEntity<List<MenuItemDTO>> getMenuTree() {
        log.debug("REST request to get menu tree");
        List<MenuItemDTO> menuItemTree = this.userService.getMenuItemTree();
        return new ResponseEntity<>(menuItemTree, HttpStatus.OK);
    }

    @Operation(summary = "api lấy danh sách chức năng dạng phẳng (không bao gồm các chức năng cha")
    @SneakyThrows
    @GetMapping("/menu/flat")
    public ResponseEntity<List<MenuItemDTO>> getMenuFlat(@RequestHeader HttpHeaders headers) {
        log.debug("REST request to get menu flat");
        List<MenuItemDTO> menuItemTree = this.userService.getMenuItemFlat(headers);
        return new ResponseEntity<>(menuItemTree, HttpStatus.OK);
    }

    @Operation(summary = "api lấy thông tin người dùng hiện tại")
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile() {
        log.debug("REST request to get user profile");
        UserDTO userProfileDTO = this.userService.getProfile();
        return ResponseEntity.ok(userProfileDTO);
    }

    @Operation(summary = "api cập nhật thông tin người dùng hiện tại")
    @PatchMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> updateUserProfile(@Validated(UserDTO.UpdateProfileCase.class) @RequestBody UserDTO userProfileDTO) {
        log.debug("REST request to update user profile");
        UserDTO updatedUserProfileDTO = this.userService.updateProfile(userProfileDTO);
        return ResponseEntity.ok(updatedUserProfileDTO);
    }

    @Operation(summary = "api cập nhật thông tin người dùng hiện tại kèm upload ảnh chữ ký")
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> updateUserProfileWithImage(
            @Validated(UserDTO.UpdateProfileCase.class) @RequestPart("profile") UserDTO userProfileDTO,
            @RequestPart(value = "image", required = false) MultipartFile signatureImageFile) {
        log.debug("REST request to update user profile with image");
        UserDTO updatedUserProfileDTO = this.userService.updateProfile(userProfileDTO, signatureImageFile);
        return ResponseEntity.ok(updatedUserProfileDTO);
    }

    @Operation(summary = "api lấy chữ ký người dùng hiện tại")
    @GetMapping(value = "/signature", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> getUserSignature() {
        log.debug("REST request to get user signature");
        Pair<String, Resource> signatureResult = this.userService.getSignature();
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_PNG)
                .eTag(StringUtils.hasText(signatureResult.getFirst()) ? signatureResult.getFirst() : null)
                .body(signatureResult.getSecond());
    }

    @Operation(summary = "api cập nhật chữ ký người dùng hiện tại")
    @PatchMapping(value = "/signature", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateUserSignature(@Valid @RequestPart("image") MultipartFile signatureImageFile) {
        log.debug("REST request to update user signature");
        this.userService.updateSignature(signatureImageFile);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "api xóa chữ ký người dùng hiện tại")
    @DeleteMapping(value = "/signature")
    public ResponseEntity<Void> deleteUserSignature() {
        log.debug("REST request to delete user signature");
        this.userService.deleteSignature();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "api khởi tạo firebase FCM token gắn với người dùng hiện tại")
    @PostMapping(value = "/fcm/init", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<ModifyResultDTO> initFcm(@RequestParam MultiValueMap<String, String> paramMap) {
        log.debug("REST request to init user FCM token");
        ModifyResultDTO modifyResultDTO = this.userService.initFcm(paramMap.getFirst("token"));
        return ResponseEntity.ok(modifyResultDTO);
    }

    @Operation(summary = "api lấy tất cả các firebase FCM token gắn với người dùng hiện tại")
    @GetMapping("/fcm/tokens")
    public ResponseEntity<List<FcmTokenDTO>> getFcmTokens() {
        log.debug("REST request to get user FCM tokens");
        List<FcmTokenDTO> fcmTokenDTOS = this.userService.getFcmTokens();
        return ResponseEntity.ok(fcmTokenDTOS);
    }

    @Operation(summary = "api quên mật khẩu (xác nhận)")
    @PostMapping("/forgot-password/confirm")
    public ResponseEntity<ModifyResultDTO> verifyToken(@RequestBody ForgotPasswordConfirmDTO forgotPasswordConfirmDTO) {
        log.debug("REST request to confirm forgot password");
        ModifyResultDTO modifyResultDTO = this.userService.forgotPasswordConfirm(forgotPasswordConfirmDTO);
        return ResponseEntity.status(HttpStatus.OK).body(modifyResultDTO);
    }

    @Operation(summary = "api đổi mật khẩu")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        log.debug("REST request to change password");
        this.userService.changePassword(changePasswordDTO);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @Operation(summary = "api kiểm tra quyền hạn")
    @PostMapping("/check-permission")
    public ResponseEntity<PermissionCheckDTO> checkPermission(@RequestBody PermissionCheckDTO permissionCheckDTO, @RequestHeader HttpHeaders httpHeaders) {
        log.debug("REST request to check currrent user Permission");
        Optional<UserDTO> currentUserOptional = SecurityUtil.getCurrentUser();
        if (currentUserOptional.isEmpty()) {
            permissionCheckDTO.setAllowed(false);
            return ResponseEntity.status(HttpStatus.OK).body(permissionCheckDTO);
        }
        deviceService.getIsMobile(httpHeaders);
        UserDTO userDTO = currentUserOptional.get();
        String appCode;
        if(deviceService.getIsMobile(httpHeaders)) {
            appCode = "vnsky-sale";
        } else {
            appCode = userDTO.getAttribute("appCode");
        }

        UserPolicyDTO userPolicyDTO = this.objectService.retrieveUserPolicy(userDTO.getId(), appCode);
        Map<String, Set<String>> policyMap = userPolicyDTO.getResourceAccess();
        List<PermissionDTO> permissionDTOList = permissionCheckDTO.getCheckPermissions();
        boolean allowed = this.objectService.checkPolicy(policyMap, permissionDTOList);
        permissionCheckDTO.setAllowed(allowed);
        return ResponseEntity.status(HttpStatus.OK).body(permissionCheckDTO);
    }

}
