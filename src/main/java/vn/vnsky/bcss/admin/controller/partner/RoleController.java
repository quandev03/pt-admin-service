package vn.vnsky.bcss.admin.controller.partner;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.vnsky.bcss.admin.constant.ResponseView;
import vn.vnsky.bcss.admin.controller.RoleControllerBase;
import vn.vnsky.bcss.admin.dto.RoleDTO;
import vn.vnsky.bcss.admin.service.RoleService;

import java.util.List;

@Slf4j
@Tag(name = "Partner Role API")
@RestController("partnerRoleController")
@RequestMapping("${application.partner-web-oAuth2-client-info.api-prefix}/api/roles")
public class RoleController extends RoleControllerBase {

    @Autowired
    public RoleController(RoleService roleService) {
        super(roleService);
    }

    @Operation(summary = "api tìm kiếm vai trò đối tác")
    @GetMapping
    @JsonView({ResponseView.QuickSearch.class})
    public ResponseEntity<Page<RoleDTO>> search(@RequestParam(value = "q", required = false) String term,
                                                @RequestParam(value = "status", required = false) Integer status,
                                                @PageableDefault Pageable pageable) {
        log.debug("REST request to quick search role (Partner Site)");
        Page<RoleDTO> rolePage = this.roleService.search(true, term, status, pageable);
        return ResponseEntity.ok(rolePage);
    }

    @Operation(summary = "api lấy tất cả vai trò đối tác")
    @GetMapping("/all")
    public ResponseEntity<List<RoleDTO>> all() {
        log.debug("REST request to get all roles (Partner Site)");
        List<RoleDTO> roleList = this.roleService.all(true);
        return ResponseEntity.ok(roleList);
    }

    @Operation(summary = "api chi tiết cả vai trò đối tác")
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> detail(@PathVariable("id") String id) {
        log.debug("REST request to get role detail (Partner Site)");
        RoleDTO role = this.roleService.detail(true, id);
        return ResponseEntity.ok(role);
    }

}
