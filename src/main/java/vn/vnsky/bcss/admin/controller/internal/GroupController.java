package vn.vnsky.bcss.admin.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import vn.vnsky.bcss.admin.controller.GroupControllerBase;
import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.GroupDTO;
import vn.vnsky.bcss.admin.entity.GroupEntity;
import vn.vnsky.bcss.admin.service.GroupService;

import java.util.List;

/**
 * REST controller for managing {@link GroupEntity}.
 */
@Slf4j
@Tag(name = "Internal Group API")
@RestController("internalGroupController")
@RequestMapping("${application.vnsky-web-oAuth2-client-info.api-prefix}/api/groups")
public class GroupController extends GroupControllerBase {

    @Autowired
    public GroupController(GroupService groupsService) {
        super(groupsService);
    }

    @Operation(summary = "api tìm kiếm nhóm người dùng của VNSKY")
    @GetMapping({"/internal"})
    public ResponseEntity<Page<GroupDTO>> search(@RequestParam(value = "q", required = false) String term,
                                                 @RequestParam(value = "status", required = false) Integer status,
                                                 @PageableDefault Pageable pageable) {
        log.debug("REST request to search internal groups");
        String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;
        Page<GroupDTO> groupDTOPage = this.groupsService.search(clientIdentity, term, status, pageable);
        return ResponseEntity.ok(groupDTOPage);
    }

    @Operation(summary = "api tìm kiếm nhóm người dùng của đối tác")
    @GetMapping({"/partner/{clientIdentity}"})
    public ResponseEntity<Page<GroupDTO>> search(@PathVariable(value = "clientIdentity") String clientIdentity,
                                                 @RequestParam(value = "q", required = false) String term,
                                                 @RequestParam(value = "status", required = false) Integer status,
                                                 @PageableDefault Pageable pageable) {
        log.debug("REST request to search partner groups");
        Page<GroupDTO> groupDTOPage = this.groupsService.search(clientIdentity, term, status, pageable);
        return ResponseEntity.ok(groupDTOPage);
    }

    @Operation(summary = "api lất tất cả nhóm người dùng của VNSKY")
    @GetMapping({"/internal/all"})
    public ResponseEntity<List<GroupDTO>> all() {
        log.debug("REST request to get all internal groups");
        String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;
        List<GroupDTO> groupsPage = this.groupsService.all(clientIdentity);
        return ResponseEntity.ok(groupsPage);
    }

    @Operation(summary = "api lất tất cả nhóm người dùng của đối tác")
    @GetMapping({"/partner/{clientIdentity}/all"})
    public ResponseEntity<List<GroupDTO>> all(@PathVariable(value = "clientIdentity") String clientIdentity) {
        log.debug("REST request to get all partner groups");
        List<GroupDTO> groupsPage = this.groupsService.all(clientIdentity);
        return ResponseEntity.ok(groupsPage);
    }

    @Operation(summary = "api xem chi tiết nhóm người dùng của VNSKY")
    @GetMapping({"/internal/{id}"})
    @AuditDetail(targetType = "INTERNAL_GROUP")
    public ResponseEntity<GroupDTO> detail(@PathVariable("id") String id) {
        log.debug("REST request to get internal group id : {}", id);
        String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;
        GroupDTO groupDTO = this.groupsService.detail(clientIdentity, id);
        return ResponseEntity.ok(groupDTO);
    }

    @Operation(summary = "api xem chi tiết nhóm người dùng của đối tác")
    @GetMapping({"/partner/{clientIdentity}/{id}"})
    @AuditDetail(targetType = "PARTNER_GROUP")
    public ResponseEntity<GroupDTO> detail(@PathVariable(value = "clientIdentity") String clientIdentity,
                                           @PathVariable("id") String id) {
        log.debug("REST request to get partner group detail id : {}", id);
        GroupDTO groupDTO = this.groupsService.detail(clientIdentity, id);
        return ResponseEntity.ok(groupDTO);
    }

    @Operation(summary = "api thêm mới nhóm người dùng của VNSKY")
    @PostMapping({"/internal"})
    @AuditAction(actionType = AuditActionType.CREATE, targetType = "INTERNAL_GROUP")
    public ResponseEntity<GroupDTO> create(@Validated({GroupDTO.CreateCase.class}) @RequestBody @Valid GroupDTO groupDTO) {
        log.debug("REST request to create an internal group : {}", groupDTO);
        String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;
        GroupDTO newGroupDTO = this.groupsService.create(clientIdentity, groupDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newGroupDTO);
    }

    @Operation(summary = "api thêm mới nhóm người dùng của đối tác")
    @PostMapping({"/partner/{clientIdentity}"})
    @AuditAction(actionType = AuditActionType.CREATE, targetType = "PARTNER_GROUP")
    public ResponseEntity<GroupDTO> create(@PathVariable(value = "clientIdentity") String clientIdentity,
                                           @Validated({GroupDTO.CreateCase.class}) @RequestBody @Valid GroupDTO groupDTO) {
        log.debug("REST request to create a partner group : {}", groupDTO);
        GroupDTO newGroupDTO = this.groupsService.create(clientIdentity, groupDTO);
        return ResponseEntity.ok(newGroupDTO);
    }

    @PutMapping({"/internal/{id}"})
    @Operation(summary = "api sửa nhóm nhóm người dùng của VNSKY")
    @AuditAction(actionType = AuditActionType.UPDATE, targetType = "INTERNAL_GROUP")
    public ResponseEntity<GroupDTO> update(@AuditId @PathVariable("id") String id, @RequestBody GroupDTO groupsDTO) {
        log.debug("REST request to update internal group id {}: {}", id, groupsDTO);
        String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;
        GroupDTO updatedGroupDTO = this.groupsService.update(clientIdentity, id, groupsDTO);
        return ResponseEntity.ok(updatedGroupDTO);
    }

    @PutMapping({"/partner/{clientIdentity}/{id}"})
    @Operation(summary = "api sửa nhóm nhóm người dùng của đối tác")
    @AuditAction(actionType = AuditActionType.UPDATE, targetType = "PARTNER_GROUP")
    public ResponseEntity<GroupDTO> update(@AuditId @PathVariable(value = "clientIdentity") String clientIdentity,
                                           @AuditId @PathVariable("id") String id, @RequestBody GroupDTO groupsDTO) {
        log.debug("REST request to update partner groups id {}: {}", id, groupsDTO);
        GroupDTO updatedGroupDTO = this.groupsService.update(clientIdentity, id, groupsDTO);
        return ResponseEntity.ok(updatedGroupDTO);
    }

    @AuditAction(actionType = AuditActionType.DELETE, targetType = "INTERNAL_GROUP")
    @Operation(summary = "api xóa nhóm người dùng của VNSKY")
    @DeleteMapping({"/internal/{id}"})
    public ResponseEntity<DeleteCountDTO> delete(@AuditId @PathVariable("id") String id) {
        log.debug("REST request to delete an internal group : {}", id);
        String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;
        DeleteCountDTO deleteCountDTO = this.groupsService.delete(clientIdentity, List.of(id));
        return ResponseEntity.ok(deleteCountDTO);
    }

    @AuditAction(actionType = AuditActionType.DELETE, targetType = "PARTNER_GROUP")
    @Operation(summary = "api xóa nhóm người dùng của đối tác")
    @DeleteMapping({"/partner/{clientIdentity}/{id}"})
    public ResponseEntity<DeleteCountDTO> delete(@AuditId @PathVariable(value = "clientIdentity") String clientIdentity,
                                                 @AuditId @PathVariable("id") String id) {
        log.debug("REST request to delete a partner group : {}", id);
        DeleteCountDTO deleteCountDTO = this.groupsService.delete(clientIdentity, List.of(id));
        return ResponseEntity.ok(deleteCountDTO);
    }

}
