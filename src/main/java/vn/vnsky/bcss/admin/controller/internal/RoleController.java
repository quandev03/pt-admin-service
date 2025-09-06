package vn.vnsky.bcss.admin.controller.internal;

import com.fasterxml.jackson.annotation.JsonView;
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
import org.springframework.web.bind.annotation.*;
import vn.vnsky.bcss.admin.annotation.AuditAction;
import vn.vnsky.bcss.admin.annotation.AuditDetail;
import vn.vnsky.bcss.admin.annotation.AuditId;
import vn.vnsky.bcss.admin.constant.AuditActionType;
import vn.vnsky.bcss.admin.constant.ResponseView;
import vn.vnsky.bcss.admin.controller.RoleControllerBase;
import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.RoleDTO;
import vn.vnsky.bcss.admin.service.RoleService;

import java.util.List;

@Slf4j
@Tag(name = "Internal Role API")
@RestController("internalRoleController")
@RequestMapping("${application.vnsky-web-oAuth2-client-info.api-prefix}/api/roles")
public class RoleController extends RoleControllerBase {

    @Autowired
    public RoleController(RoleService roleService) {
        super(roleService);
    }

    @Operation(summary = "api tìm kiếm vai trò VNSKY theo name")
    @GetMapping("/internal")
    @JsonView({ResponseView.QuickSearch.class})
    public ResponseEntity<Page<RoleDTO>> searchInternal(@RequestParam(value = "q", required = false) String term,
                                                        @RequestParam(value = "status", required = false) Integer status,
                                                        @PageableDefault Pageable pageable) {
        log.debug("REST request to quick search internal role (Internal Site)");
        Page<RoleDTO> rolePage = this.roleService.search(false, term, status, pageable);
        return ResponseEntity.ok(rolePage);
    }

    @Operation(summary = "api lấy tất cả vai trò VNSKY")
    @GetMapping("/internal/all")
    public ResponseEntity<List<RoleDTO>> allInternal() {
        log.debug("REST request to get all internal roles (Internal Site)");
        List<RoleDTO> roleList = this.roleService.all(false);
        return ResponseEntity.ok(roleList);
    }

    @Operation(summary = "api thêm mới vai trò VNSKY")
    @AuditAction(actionType = AuditActionType.CREATE, targetType = "INTERNAL_ROLE")
    @PostMapping("/internal")
    public ResponseEntity<RoleDTO> createRoleInternal(@Valid @RequestBody RoleDTO roleDTO) {
        log.debug("REST request to create internal role (Internal Site)");
        RoleDTO createdRole = this.roleService.create(false, false, roleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    @Operation(summary = "api chi tiết vai trò VNSKY")
    @GetMapping("/internal/{id}")
    @AuditDetail(targetType = "INTERNAL_ROLE")
    public ResponseEntity<RoleDTO> roleDetailInternal(@PathVariable("id") String id) {
        log.debug("REST request to get internal role detail (Internal Site)");
        RoleDTO role = this.roleService.detail(false, id);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "api cập nhật vai trò VNSKY")
    @AuditAction(actionType = AuditActionType.UPDATE, targetType = "INTERNAL_ROLE")
    @PutMapping("/internal/{id}")
    public ResponseEntity<RoleDTO> updateRoleInternal(@AuditId @PathVariable("id") String roleId, @Valid @RequestBody RoleDTO roleDTO) {
        log.debug("REST request to update internal role (Internal Site)");
        RoleDTO updatedRole = this.roleService.update(false, false, roleId, roleDTO);
        return ResponseEntity.ok(updatedRole);
    }

    @Operation(summary = "api xóa vai trò VNSKY")
    @AuditAction(actionType = AuditActionType.DELETE, targetType = "INTERNAL_ROLE")
    @DeleteMapping("/internal/{id}")
    public ResponseEntity<DeleteCountDTO> deleteRoleInternal(@AuditId @PathVariable("id") String id) {
        log.debug("REST request to delete internal role (Partner Site)");
        DeleteCountDTO deleteCountDTO = this.roleService.delete(false, List.of(id));
        return ResponseEntity.ok(deleteCountDTO);
    }

    @Operation(summary = "api tìm kiếm vai trò đối tác theo name")
    @GetMapping("/partner")
    @JsonView({ResponseView.QuickSearch.class})
    public ResponseEntity<Page<RoleDTO>> searchPartner(@RequestParam(value = "q", required = false) String term,
                                                       @RequestParam(value = "status", required = false) Integer status,
                                                       @PageableDefault Pageable pageable) {
        log.debug("REST request to quick search partner role (Internal Site)");
        Page<RoleDTO> rolePage = this.roleService.search(true, term, status, pageable);
        return ResponseEntity.ok(rolePage);
    }

    @Operation(summary = "api lấy tất cả vai trò đối tác")
    @GetMapping("/partner/all")
    public ResponseEntity<List<RoleDTO>> allPartner() {
        log.debug("REST request to get all partner roles (Internal Site)");
        List<RoleDTO> roleList = this.roleService.all(true);
        return ResponseEntity.ok(roleList);
    }

    @Operation(summary = "api thêm mới vai trò đối tác")
    @AuditAction(actionType = AuditActionType.CREATE, targetType = "PARTNER_ROLE")
    @PostMapping("/partner")
    public ResponseEntity<RoleDTO> createRolePartner(@RequestParam(required = false) Boolean isMobile, @Valid @RequestBody RoleDTO roleDTO) {
        log.debug("REST request to create partner role (Internal Site)");
        RoleDTO createdRole = this.roleService.create(true, isMobile, roleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    @Operation(summary = "api chi tiết vai trò đối tác")
    @GetMapping("/partner/{id}")
    @AuditDetail(targetType = "PARTNER_ROLE")
    public ResponseEntity<RoleDTO> roleDetailPartner(@PathVariable("id") String id) {
        log.debug("REST request to get partner role detail (Internal Site)");
        RoleDTO role = this.roleService.detail(true, id);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "api cập nhật vai trò đối tác")
    @AuditAction(actionType = AuditActionType.UPDATE, targetType = "PARTNER_ROLE")
    @PutMapping("/partner/{id}")
    public ResponseEntity<RoleDTO> updateRolePartner(@AuditId @PathVariable("id") String roleId,
                                                     @RequestParam(required = false) Boolean isMobile, @Valid @RequestBody RoleDTO roleDTO) {
        log.debug("REST request to update partner role (Internal Site)");
        RoleDTO updatedRole = this.roleService.update(true, isMobile, roleId, roleDTO);
        return ResponseEntity.ok(updatedRole);
    }

    @Operation(summary = "api xóa vai trò đối tác")
    @AuditAction(actionType = AuditActionType.DELETE, targetType = "PARTNER_ROLE")
    @DeleteMapping("/partner/{id}")
    public ResponseEntity<DeleteCountDTO> deleteRolePartner(@AuditId @PathVariable("id") String id) {
        log.debug("REST request to delete partner role (Partner Site)");
        DeleteCountDTO deleteCountDTO = this.roleService.delete(true, List.of(id));
        return ResponseEntity.ok(deleteCountDTO);
    }
}
