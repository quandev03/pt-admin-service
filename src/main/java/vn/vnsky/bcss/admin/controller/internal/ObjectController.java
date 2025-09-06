package vn.vnsky.bcss.admin.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.vnsky.bcss.admin.dto.CreateUpdateObjectDTO;
import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.ObjectResponseDTO;
import vn.vnsky.bcss.admin.service.ObjectService;

import java.util.List;

@Slf4j
@Tag(name = "Object API")
@RestController("internalObjectController")
@RequestMapping("${application.vnsky-web-oAuth2-client-info.api-prefix}/api/objects")
public class ObjectController {

    private final ObjectService objectService;

    @Autowired
    public ObjectController(ObjectService objectService) {
        this.objectService = objectService;
    }

    @Operation(summary = "api chi tiết chức năng")
    @GetMapping("/{id}")
    public ResponseEntity<ObjectResponseDTO> findById(@PathVariable String id,
                                                      @RequestParam(value = "isPartner", defaultValue = "false") boolean isPartner,
                                                      @RequestParam(value = "isMobile", required = false) Boolean isMobile) {
        log.debug("REST request to get object detail (Internal Site)");
        ObjectResponseDTO objectResponseDTO = this.objectService.findById(id, isPartner, isMobile);
        return ResponseEntity.ok(objectResponseDTO);
    }

    @Operation(summary = "api danh sách chức năng hình cây")
    @GetMapping
    public ResponseEntity<List<ObjectResponseDTO>> find(@RequestParam(value = "isPartner", defaultValue = "false") boolean isPartner,
                                                        @RequestParam(value = "isMobile", required = false) Boolean isMobile,
                                                        @RequestHeader HttpHeaders headers) {
        log.debug("REST request get all objects (Internal Site)");
        List<ObjectResponseDTO> objectTree = this.objectService.tree(isPartner, isMobile, headers);
        return ResponseEntity.ok(objectTree);
    }

    @Operation(summary = "api tạo chức năng")
    @PostMapping()
    public ResponseEntity<ObjectResponseDTO> create(@Validated @RequestBody CreateUpdateObjectDTO createUpdateObjectDTO) {
        log.debug("REST request to create object (Internal Site)");
        ObjectResponseDTO objectResponseDTO = this.objectService.create(createUpdateObjectDTO);
        return ResponseEntity.ok(objectResponseDTO);
    }

    @Operation(summary = "api cập nhật chức năng")
    @PutMapping("/{id}")
    public ResponseEntity<ObjectResponseDTO> update(@PathVariable String id,
                                                    @Validated @RequestBody CreateUpdateObjectDTO createUpdateObjectDTO) {
        log.debug("REST request to update notification (Internal Site)");
        ObjectResponseDTO objectResponseDTO = this.objectService.update(id, createUpdateObjectDTO);
        return ResponseEntity.ok(objectResponseDTO);
    }

    @Operation(summary = "api xóa chức năng")
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteCountDTO> delete(@PathVariable String id,
                                                 @RequestParam(value = "isPartner", defaultValue = "false") boolean isPartner,
                                                 @RequestParam(value = "isMobile", required = false) Boolean isMobile) {
        log.debug("REST request to delete notification (Internal Site)");
        DeleteCountDTO deleteCountDTO = this.objectService.delete(id, isPartner, isMobile);
        return ResponseEntity.ok(deleteCountDTO);
    }

}
