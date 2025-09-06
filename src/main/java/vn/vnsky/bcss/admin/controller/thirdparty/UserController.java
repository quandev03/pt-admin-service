package vn.vnsky.bcss.admin.controller.thirdparty;


import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.vnsky.bcss.admin.annotation.AuditAction;
import vn.vnsky.bcss.admin.annotation.AuditDetail;
import vn.vnsky.bcss.admin.annotation.AuditId;
import vn.vnsky.bcss.admin.constant.AuditActionType;
import vn.vnsky.bcss.admin.constant.ResponseView;
import vn.vnsky.bcss.admin.controller.UserControllerBase;
import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;
import vn.vnsky.bcss.admin.service.UserService;
import vn.vnsky.bcss.admin.util.SecurityUtil;

import java.util.List;

@Slf4j
@Tag(name = "Third Party User API")
@RestController("thirdPartyUserController")
@RequestMapping("${application.third-party-oAuth2-client-info.api-prefix}/private/api/users")
public class UserController extends UserControllerBase {

    @Autowired
    public UserController(UserService userService) {
        super(userService);
    }

    @Operation(summary = "api tìm kiếm người dùng của bên thứ 3")
    @GetMapping
    public ResponseEntity<Page<UserDTO>> search(@RequestParam(value = "q", required = false) String term,
                                                @RequestParam(value = "status", required = false) Integer status,
                                                @PageableDefault Pageable pageable) {
        log.debug("REST request to search users (Third Party)");
        String clientIdentity = SecurityUtil.getCurrentClientId();
        Page<UserDTO> userSuggestPage = this.userService.search(clientIdentity, term, status, pageable);
        return ResponseEntity.ok(userSuggestPage);
    }

    @JsonView(ResponseView.GroupUserSuggest.class)
    @Operation(summary = "api lấy tất cả các người dùng của bên thứ 3")
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> all(
            @RequestParam(value = "permission", required = false) List<String> permissions,
            @RequestParam(value = "departmentCode", required = false) String departmentCode,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "sort", defaultValue = "alphabet", required = false) String sort) {
        log.debug("REST request to get all users (Third Party)");
        String clientIdentity = SecurityUtil.getCurrentClientId();
        List<UserDTO> userSuggestPage = this.userService.all(clientIdentity, permissions, departmentCode, active, sort);
        return ResponseEntity.ok(userSuggestPage);
    }

    @Operation(summary = "api tạo người dùng của bên thứ 3")
    @PostMapping
    @AuditAction(actionType = AuditActionType.CREATE, targetType = "THIRD_PARTY_USER")
    public ResponseEntity<UserDTO> create(@RequestBody @Validated(UserDTO.CreateCase.class) UserDTO userDTO) {
        String clientIdentity = SecurityUtil.getCurrentClientId();
        UserDTO createdUserDTO = this.userService.create(clientIdentity, userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUserDTO);
    }

    @Operation(summary = "api xem chi tiết người dùng của bên thứ 3")
    @GetMapping("/{id}")
    @JsonView(ResponseView.Internal.class)
    public ResponseEntity<UserDTO> detail(@PathVariable("id") String userId) {
        log.debug("REST request to get user detail {} (Internal Site)", userId);
        String clientIdentity = SecurityUtil.getCurrentClientId();
        UserDTO userDetail = this.userService.detail(clientIdentity, userId);
        return ResponseEntity.status(HttpStatus.OK).body(userDetail);
    }

    @Operation(summary = "api cập nhật người dùng của bên thứ 3")
    @PutMapping("/{id}")
    @AuditAction(actionType = AuditActionType.UPDATE, targetType = "THIRD_PARTY_USER")
    public ResponseEntity<UserDTO> update(@AuditId @PathVariable("id") String id,
                                          @RequestBody @Validated(UserDTO.UpdateCase.class) UserDTO dto) {
        String clientIdentity = SecurityUtil.getCurrentClientId();
        UserDTO updatedUserDTO = this.userService.update(clientIdentity, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUserDTO);
    }

    @Operation(summary = "api xóa người dùng của bên thứ 3")
    @DeleteMapping("/{id}")
    @AuditAction(actionType = AuditActionType.DELETE, targetType = "THIRD_PARTY_USER")
    public ResponseEntity<DeleteCountDTO> delete(@AuditId @PathVariable("id") String id) {
        String clientIdentity = SecurityUtil.getCurrentClientId();
        DeleteCountDTO deleteCountDTO = this.userService.delete(clientIdentity, List.of(id));
        return ResponseEntity.ok(deleteCountDTO);
    }

    @Operation(summary = "api lấy thông tin chi tiết người dùng của đối tác")
    @GetMapping({"/partner/internal/{clientIdentity}/{id}"})
    @JsonView(ResponseView.Internal.class)
    @AuditDetail(targetType = "PARTNER_USER")
    public ResponseEntity<UserDTO> detailInternalUse(@PathVariable(value = "clientIdentity") String clientIdentity,
                                                     @PathVariable("id") String userId) {
        log.debug("REST request to get partner user detail {} (Internal Site) for be call", userId);
        UserDTO userDetail = this.userService.detail(clientIdentity, userId);
        return ResponseEntity.status(HttpStatus.OK).body(userDetail);
    }
}
