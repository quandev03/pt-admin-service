package vn.vnsky.bcss.admin.controller.internal;


import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.constant.ResponseView;
import vn.vnsky.bcss.admin.controller.UserControllerBase;
import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;
import vn.vnsky.bcss.admin.service.UserService;

import java.util.List;

@Slf4j
@Tag(name = "Internal User API")
@RestController("internalUserController")
@RequestMapping("${application.vnsky-web-oAuth2-client-info.api-prefix}/api/users")
public class UserController extends UserControllerBase {

    @Autowired
    public UserController(UserService userService) {
        super(userService);
    }

    @GetMapping({"/internal"})
    @Operation(summary = "api tìm kiếm người dùng của VNSKY")
    public ResponseEntity<Page<UserDTO>> search(@RequestParam(value = "q", required = false) String term,
                                                @RequestParam(value = "status", required = false) Integer status,
                                                @PageableDefault Pageable pageable) {
        log.debug("REST request to search internal users (Internal Site)");
        String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;
        Page<UserDTO> userSuggestPage = this.userService.search(clientIdentity, term, status, pageable);
        return ResponseEntity.ok(userSuggestPage);
    }

    @GetMapping({"/partner/{clientIdentity}"})
    @Operation(summary = "api tìm kiếm người dùng của đối tác")
    public ResponseEntity<Page<UserDTO>> search(@PathVariable(value = "clientIdentity") String clientIdentity,
                                                @RequestParam(value = "q", required = false) String term,
                                                @RequestParam(value = "status", required = false) Integer status,
                                                @PageableDefault Pageable pageable) {
        log.debug("REST request to search partner users (Internal Site)");
        Page<UserDTO> userSuggestPage = this.userService.search(clientIdentity, term, status, pageable);
        return ResponseEntity.ok(userSuggestPage);
    }

    @JsonView(ResponseView.GroupUserSuggest.class)
    @Operation(summary = "api lấy tất cả các người dùng của VNSKY")
    @GetMapping({"/internal/all"})
    public ResponseEntity<List<UserDTO>> all(@RequestParam(value = "permission", required = false) List<String> permissions,
                                             @RequestParam(value = "departmentCode", required = false) String departmentCode,
                                             @RequestParam(value = "active", required = false) Boolean active,
                                             @RequestParam(value = "sort", defaultValue = "alphabet", required = false) String sort) {
        log.debug("REST request to get all internal users (Internal Site)");
        String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;
        List<UserDTO> userSuggestPage = this.userService.all(clientIdentity, permissions, departmentCode, active, sort);
        return ResponseEntity.ok(userSuggestPage);
    }

    @JsonView(ResponseView.GroupUserSuggest.class)
    @Operation(summary = "api lấy tất cả các người dùng (VNSKY và đối tác)")
    @GetMapping({"/all"})
    public ResponseEntity<List<UserDTO>> all(@RequestParam(value = "isPartner", required = false) Boolean isPartner,
                                             @RequestParam(value = "q", required = false) String term) {
        log.debug("REST request to get all users (Internal Site)");
        List<UserDTO> userSuggestPage = this.userService.all(isPartner, term);
        return ResponseEntity.ok(userSuggestPage);
    }

    @JsonView(ResponseView.GroupUserSuggest.class)
    @Operation(summary = "api lấy tất cả các người dùng của đối tác")
    @GetMapping({"/partner/{clientIdentity}/all"})
    public ResponseEntity<List<UserDTO>> all(@PathVariable(value = "clientIdentity") String clientIdentity,
                                             @RequestParam(value = "permission", required = false) List<String> permissions,
                                             @RequestParam(value = "departmentCode", required = false) String departmentCode,
                                             @RequestParam(value = "active", required = false) Boolean active,
                                             @RequestParam(value = "sort", defaultValue = "alphabet", required = false) String sort) {
        log.debug("REST request to get all partner users (Internal Site)");
        List<UserDTO> userSuggestPage = this.userService.all(clientIdentity, permissions, departmentCode, active, sort);
        return ResponseEntity.ok(userSuggestPage);
    }

    @Operation(summary = "api lấy thông tin chi tiết người dùng của VNSKY")
    @GetMapping({"/internal/{id}"})
    @JsonView(ResponseView.Internal.class)
    @AuditDetail(targetType = "INTERNAL_USER")
    public ResponseEntity<UserDTO> detail(@AuditId @PathVariable("id") String userId) {
        log.debug("REST request to get internal user detail {} (Internal Site)", userId);
        String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;
        UserDTO userDetail = this.userService.detail(clientIdentity, userId);
        return ResponseEntity.status(HttpStatus.OK).body(userDetail);
    }

    @Operation(summary = "api lấy thông tin chi tiết người dùng của đối tác")
    @GetMapping({"/partner/{clientIdentity}/{id}"})
    @JsonView(ResponseView.Internal.class)
    @AuditDetail(targetType = "PARTNER_USER")
    public ResponseEntity<UserDTO> detail(@PathVariable(value = "clientIdentity") String clientIdentity,
                                          @PathVariable("id") String userId) {
        log.debug("REST request to get partner user detail {} (Internal Site)", userId);
        UserDTO userDetail = this.userService.detail(clientIdentity, userId);
        return ResponseEntity.status(HttpStatus.OK).body(userDetail);
    }

    @Operation(summary = "api tạo người dùng của VNSKY")
    @PostMapping({"/internal"})
    @AuditAction(actionType = AuditActionType.CREATE, targetType = "INTERNAL_USER")
    public ResponseEntity<UserDTO> create(@RequestBody @Validated(UserDTO.CreateCase.class) UserDTO userDTO) {
        String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;
        UserDTO createdUserDTO = this.userService.create(clientIdentity, userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUserDTO);
    }

    @Operation(summary = "api tạo người dùng của đối tác")
    @PostMapping({"/partner/{clientIdentity}"})
    @AuditAction(actionType = AuditActionType.CREATE, targetType = "PARTNER_USER")
    public ResponseEntity<UserDTO> create(@PathVariable(value = "clientIdentity") String clientIdentity,
                                          @RequestBody @Validated(UserDTO.CreateCase.class) UserDTO userDTO) {
        UserDTO createdUserDTO = this.userService.create(clientIdentity, userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUserDTO);
    }

    @Operation(summary = "api cập nhật người dùng của VNSKY")
    @PutMapping("/internal/{id}")
    @AuditAction(actionType = AuditActionType.UPDATE, targetType = "INTERNAL_USER")
    public ResponseEntity<UserDTO> update(@AuditId @PathVariable("id") String id,
                                          @RequestBody @Validated(UserDTO.UpdateCase.class) UserDTO dto) {
        String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;
        UserDTO updatedUserDTO = this.userService.update(clientIdentity, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUserDTO);
    }

    @Operation(summary = "api cập nhật người dùng của đối tác")
    @PutMapping("/partner/{clientIdentity}/{id}")
    @AuditAction(actionType = AuditActionType.UPDATE, targetType = "PARTNER_USER")
    public ResponseEntity<UserDTO> update(@AuditId @PathVariable(value = "clientIdentity") String clientIdentity,
                                          @AuditId @PathVariable("id") String id,
                                          @RequestBody @Validated(UserDTO.UpdateCase.class) UserDTO dto) {
        UserDTO updatedUserDTO = this.userService.update(clientIdentity, id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUserDTO);
    }

    @Operation(summary = "api xóa người dùng của VNSKY")
    @DeleteMapping("/internal/{id}")
    @AuditAction(actionType = AuditActionType.DELETE, targetType = "INTERNAL_USER")
    public ResponseEntity<DeleteCountDTO> delete(@AuditId @PathVariable("id") String id) {
        String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;
        DeleteCountDTO deleteCountDTO = this.userService.delete(clientIdentity, List.of(id));
        return ResponseEntity.ok(deleteCountDTO);
    }

    @Operation(summary = "api xóa người dùng của đối tác")
    @DeleteMapping("/partner/{clientIdentity}/{id}")
    @AuditAction(actionType = AuditActionType.DELETE, targetType = "PARTNER_USER")
    public ResponseEntity<DeleteCountDTO> delete(@AuditId @PathVariable(value = "clientIdentity") String clientIdentity,
                                                 @AuditId @PathVariable("id") String id) {
        DeleteCountDTO deleteCountDTO = this.userService.delete(clientIdentity, List.of(id));
        return ResponseEntity.ok(deleteCountDTO);
    }

    @Operation(summary = "Api tìm kiếm người dùng của tất cả đối tác")
    @GetMapping("/partner/search")
    @Parameter(name = "size", description = "Số lượng bản ghi trong 1 trang")
    @Parameter(name ="page", description = "Vị trí trang, bắt đầu từ 0")
    public ResponseEntity<Page<UserDTO>> searchPartnerUsers(@RequestParam(value = "q", required = false) String term,
                                                            @RequestParam(value = "status", required = false) Integer status,
                                                            @Parameter(hidden = true) @PageableDefault Pageable pageable){
        return ResponseEntity.ok(userService.searchPartnerUsers(term, status, pageable));
    }

    @Operation(summary = "Api tìm kiếm danh sách user theo mã object")
    @PostMapping("/search-by-object-code")
    public ResponseEntity<List<String>> searchUsersOfObject(@RequestBody List<String> objectCodes){
        return ResponseEntity.ok(userService.searchUserByObjectCode(objectCodes));
    }

    @Operation(summary = "API tìm kiếm user theo clientid và userName")
    @GetMapping("/search-by-clientid-and-username")
    public ResponseEntity<UserDTO> searchByClientIdAndUsername(@RequestParam("clientId") String clientId,
                                                               @RequestParam("username") String username) {
        return ResponseEntity.ok(userService.loadUserByClientAndUser(clientId, username));
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

    @GetMapping({"/partner/{clientIdentity}/internal"})
    @Operation(summary = "api tìm kiếm người dùng của đối tác")
    public ResponseEntity<Page<UserDTO>> searchForInternalUse(@PathVariable(value = "clientIdentity") String clientIdentity,
                                                @RequestParam(value = "q", required = false) String term,
                                                @RequestParam(value = "status", required = false) Integer status,
                                                @PageableDefault Pageable pageable) {
        log.debug("REST request to search partner users (Internal Site) using with be");
        Page<UserDTO> userSuggestPage = this.userService.search(clientIdentity, term, status, pageable);
        return ResponseEntity.ok(userSuggestPage);
    }

}
