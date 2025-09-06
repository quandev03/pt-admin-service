package vn.vnsky.bcss.admin.controller;

import com.vnsky.common.constant.ExtendedMediaType;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.vnsky.bcss.admin.dto.*;
import vn.vnsky.bcss.admin.service.ObjectService;

@Slf4j
@Tag(name = "OAuth2 Policy API")
@RestController
@RequestMapping({"/oauth2/policy",
        "${application.vnsky-web-oAuth2-client-info.api-prefix}/oauth2/policy",
        "${application.partner-web-oAuth2-client-info.api-prefix}/oauth2/policy"})
public class PolicyController {

    private final ObjectService objectService;

    @Autowired
    public PolicyController(ObjectService objectService) {
        this.objectService = objectService;
    }

    @PostMapping("/api-catalogs/register/self")
    public ResponseEntity<Void> registerApiSet() {
        this.objectService.registerSelfApiSet();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api-catalogs/register")
    public ResponseEntity<Void> registerApiSet(@RequestBody ApiCatalogRegistrationDTO apiCatalogRegistration) {
        this.objectService.registerApiSet(apiCatalogRegistration);
        return ResponseEntity.ok().build();
    }

    @PostMapping({"/check", "/check/v2"})
    public ResponseEntity<PolicyCheckDTO> checkPolicy(@RequestBody PolicyCheckDTO checkAclRequest) {
        PolicyCheckDTO apiAclResponse = this.objectService.checkPolicy(checkAclRequest);
        return ResponseEntity.ok(apiAclResponse);
    }

    @GetMapping("/cache-config")
    public ResponseEntity<PolicyCacheConfigDTO> getCacheConfig() {
        PolicyCacheConfigDTO policyCacheConfig = this.objectService.getCacheConfig();
        return ResponseEntity.ok(policyCacheConfig);
    }

    @PostMapping("/cache-config")
    public ResponseEntity<PolicyCacheConfigDTO> updateCacheConfig(@RequestBody PolicyCacheConfigDTO policyCacheConfig) {
        policyCacheConfig = this.objectService.updateCacheConfig(policyCacheConfig);
        return ResponseEntity.ok(policyCacheConfig);
    }

    @PatchMapping("/acl")
    public ResponseEntity<Void> updateAcl() {
        this.objectService.updateObjectActionName();
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/upload-acl-policy", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadAclPolicy(@RequestPart MultipartFile file) {
        Resource resultFile = objectService.uploadAclPolicy(file);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename("ket qua thuc hien upload")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .header(HttpHeaders.CONTENT_TYPE, ExtendedMediaType.APPLICATION_XLSX_VALUE)
                .body(resultFile);
    }
}
