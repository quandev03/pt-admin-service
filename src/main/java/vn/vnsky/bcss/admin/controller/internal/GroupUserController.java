package vn.vnsky.bcss.admin.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.vnsky.bcss.admin.dto.DeleteCountDTO;
import vn.vnsky.bcss.admin.dto.GroupUserExistResponseDTO;
import vn.vnsky.bcss.admin.dto.GroupUserRequestDTO;
import vn.vnsky.bcss.admin.dto.GroupUserResponseDTO;
import vn.vnsky.bcss.admin.service.GroupUserService;

@Slf4j
@Validated
@Tag(name = "Internal Group User API")
@RestController
@RequestMapping("${application.vnsky-web-oAuth2-client-info.api-prefix}/api/group-users")
public class GroupUserController {

    private final GroupUserService groupUserService;

    public GroupUserController(GroupUserService groupUserService) {
        this.groupUserService = groupUserService;
    }

    @Operation(summary = "Kiểm tra group_user tồn tại theo groupId và userId")
    @GetMapping("/exists")
    public ResponseEntity<GroupUserExistResponseDTO> exists(@RequestParam("groupId") @NotBlank String groupId,
                                                            @RequestParam("userId") @NotBlank String userId) {
        log.debug("REST request to check group user exists groupId={}, userId={}", groupId, userId);
        boolean exists = groupUserService.exists(groupId, userId);
        return ResponseEntity.ok(new GroupUserExistResponseDTO(exists));
    }

    @Operation(summary = "Tạo group_user mới khi chưa tồn tại")
    @PostMapping
    public ResponseEntity<GroupUserResponseDTO> create(@Valid @RequestBody GroupUserRequestDTO request) {
        log.debug("REST request to create group user groupId={}, userId={}", request.getGroupId(), request.getUserId());
        GroupUserResponseDTO response = groupUserService.create(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Xoá group_user theo groupId và userId")
    @DeleteMapping
    public ResponseEntity<DeleteCountDTO> delete(@Valid @RequestBody GroupUserRequestDTO request) {
        log.debug("REST request to delete group user groupId={}, userId={}", request.getGroupId(), request.getUserId());
        DeleteCountDTO deleteCountDTO = groupUserService.delete(request);
        return ResponseEntity.ok(deleteCountDTO);
    }
}

