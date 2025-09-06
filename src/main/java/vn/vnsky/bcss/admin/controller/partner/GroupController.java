package vn.vnsky.bcss.admin.controller.partner;

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
import vn.vnsky.bcss.admin.controller.GroupControllerBase;
import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.GroupDTO;
import vn.vnsky.bcss.admin.entity.GroupEntity;
import vn.vnsky.bcss.admin.service.GroupService;
import vn.vnsky.bcss.admin.util.SecurityUtil;

import java.util.List;

/**
 * REST controller for managing {@link GroupEntity}.
 */
@Slf4j
@Tag(name = "Partner Group API")
@RestController("parterGroupController")
@RequestMapping("${application.partner-web-oAuth2-client-info.api-prefix}/api/groups")
public class GroupController extends GroupControllerBase {

    @Autowired
    public GroupController(GroupService groupsService) {
        super(groupsService);
    }

    /**
     * {@code GET  /groups} : get all the groups.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of groups in body.
     */
    @Operation(summary = "api tìm kiếm nhóm tài khoản của đối tác hiện tại")
    @GetMapping
    public ResponseEntity<Page<GroupDTO>> search(@RequestParam(value = "q", required = false) String term,
                                                       @RequestParam(value = "status", required = false) Integer status,
                                                       @PageableDefault Pageable pageable) {
        log.debug("REST request to search current partner's groups");
        String clientIdentity = SecurityUtil.getCurrentClientId();
        Page<GroupDTO> groupDTOPage = this.groupsService.search(clientIdentity, term, status, pageable);
        return ResponseEntity.ok(groupDTOPage);
    }

    @Operation(summary = "api lấy tất cả nhóm người dùng của đối tác hiện tại")
    @GetMapping("/all")
    public ResponseEntity<List<GroupDTO>> all() {
        log.debug("REST request to get all current partner's groups");
        String clientIdentity = SecurityUtil.getCurrentClientId();
        List<GroupDTO> groupsPage = this.groupsService.all(clientIdentity);
        return ResponseEntity.ok(groupsPage);
    }

    /**
     * {@code GET  /groups/:id} : get the "id" groups.
     *
     * @param id the id of the groupsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the groupsDTO, or with status {@code 404 (Not Found)}.
     */
    @Operation(summary = "api xem chi tiết nhóm người dùng của đối tác hiện tại")
    @GetMapping("/{id}")
    @AuditDetail(targetType = "PARTNER_GROUP")
    public ResponseEntity<GroupDTO> detail(@PathVariable("id") String id) {
        log.debug("REST request to get current partner's groups by id : {}", id);
        String clientIdentity = SecurityUtil.getCurrentClientId();
        GroupDTO groupDTO = this.groupsService.detail(clientIdentity, id);
        return ResponseEntity.ok(groupDTO);
    }

    /**
     * {@code POST  /groups} : Create a new groups.
     *
     * @param groupsDTO the groupsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new groupsDTO, or with status {@code 400 (Bad Request)} if the groups has already an ID.
     */
    @Operation(summary = "api thêm mới nhóm người dùng của đối tác hiện tại")
    @PostMapping
    @AuditAction(actionType = AuditActionType.CREATE, targetType = "PARTNER_GROUP")
    public ResponseEntity<GroupDTO> create(@Validated({GroupDTO.CreateCase.class}) @RequestBody @Valid GroupDTO groupsDTO) {
        log.debug("REST request to save current partner's groups : {}", groupsDTO);
        String clientIdentity = SecurityUtil.getCurrentClientId();
        GroupDTO newGroupDTO = this.groupsService.create(clientIdentity, groupsDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newGroupDTO);
    }

    /**
     * {@code PUT  /groups} : Updates an existing groups.
     *
     * @param groupsDTO the groupsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated groupsDTO,
     * or with status {@code 400 (Bad Request)} if the groupsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the groupsDTO couldn't be updated.
     */
    @PutMapping("/{id}")
    @Operation(summary = "api sửa nhóm người dùng của đối tác hiện tại")
    @AuditAction(actionType = AuditActionType.UPDATE, targetType = "PARTNER_GROUP")
    public ResponseEntity<GroupDTO> update(@AuditId @PathVariable("id") String id, @RequestBody GroupDTO groupsDTO) {
        log.debug("REST request to update current partner's groups by id {}: {}", id, groupsDTO);
        String clientIdentity = SecurityUtil.getCurrentClientId();
        GroupDTO updatedGroupDTO = this.groupsService.update(clientIdentity, id, groupsDTO);
        return ResponseEntity.ok(updatedGroupDTO);
    }

    /**
     * {@code DELETE  /groups/:id} : delete the "id" groups.
     *
     * @param id the id of the groupsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(summary = "api xóa nhóm người dùng của đối tác hiện tại")
    @DeleteMapping("/{id}")
    @AuditAction(actionType = AuditActionType.DELETE, targetType = "PARTNER_GROUP")
    public ResponseEntity<DeleteCountDTO> delete(@AuditId @PathVariable("id") String id) {
        log.debug("REST request to delete a current partner's group : {}", id);
        String clientIdentity = SecurityUtil.getCurrentClientId();
        DeleteCountDTO deleteCountDTO = this.groupsService.delete(clientIdentity, List.of(id));
        return ResponseEntity.ok(deleteCountDTO);
    }

}
