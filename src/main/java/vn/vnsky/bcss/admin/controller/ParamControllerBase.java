package vn.vnsky.bcss.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.vnsky.bcss.admin.dto.ParamDTO;
import vn.vnsky.bcss.admin.service.ParamService;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "Params API")
@RestController("paramController")
@RequestMapping({"${application.vnsky-web-oAuth2-client-info.api-prefix}/api/param",
        "${application.partner-web-oAuth2-client-info.api-prefix}/api/param"})
public class ParamControllerBase {

    private final ParamService paramService;

    public ParamControllerBase(ParamService paramService) {
        this.paramService = paramService;
    }

    @Operation(summary = "api lấy giá trị tham số hệ thống theo mã")
    @GetMapping("/{code}")
    public ResponseEntity<List<ParamDTO>> getParams(@PathVariable String code, @RequestHeader(HttpHeaders.ACCEPT_LANGUAGE) String language) {
        List<ParamDTO> paramDTOList = this.paramService.getParamByCode(code, language);
        return ResponseEntity.ok(paramDTOList);
    }

    @Operation(summary = "api lấy danh sách giá trị tham số hệ thống")
    @GetMapping
    public ResponseEntity<Map<String, List<ParamDTO>>> getMultipleParams(@RequestHeader(HttpHeaders.ACCEPT_LANGUAGE) String language) {
        String redisKey = this.paramService.getKey("MULTIPLE", language);
        return ResponseEntity.ok(paramService.getMultipleParams(language, redisKey));
    }

    @Operation(summary = "api lấy các link của hệ thống")
    @GetMapping("/system-urls")
    public ResponseEntity<List<ParamDTO>> getSystemUrls() {
        List<ParamDTO> paramDTOS = this.paramService.getSystemUrls();
        return ResponseEntity.ok(paramDTOS);
    }

}
