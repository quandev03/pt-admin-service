package vn.vnsky.bcss.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vnsky.bcss.admin.dto.DepartmentDTO;
import vn.vnsky.bcss.admin.service.DepartmentService;

import java.util.List;

@Slf4j
@Tag(name = "Department API")
@RestController("departmentController")
@RequestMapping({"${application.vnsky-web-oAuth2-client-info.api-prefix}/api/departments",
        "${application.partner-web-oAuth2-client-info.api-prefix}/api/departments"})
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @Operation(summary = "api danh sách phòng ban")
    @GetMapping("/all")
    public List<DepartmentDTO> all() {
        return this.departmentService.all();
    }

    @Operation(summary = "api lấy phòng ban bởi department code")
    @GetMapping("/{departmentCode}")
    public DepartmentDTO getByDepartmentCode(@PathVariable String departmentCode) {
        return this.departmentService.findByDepartmentCode(departmentCode);
    }
}
