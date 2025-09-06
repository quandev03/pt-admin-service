package vn.vnsky.bcss.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import vn.vnsky.bcss.admin.annotation.AuditAction;
import vn.vnsky.bcss.admin.constant.AuditActionType;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.dto.ActionDTO;
import vn.vnsky.bcss.admin.service.ActionService;

import java.util.List;

@Slf4j
public abstract class ActionControllerBase {

    protected final ActionService actionService;

    protected ActionControllerBase(ActionService actionService) {
        this.actionService = actionService;
    }

    @Operation(summary = "api danh sách hành động")
    @GetMapping
    protected List<ActionDTO> getActionList() {
        return actionService.getActionList();
    }

    @PostMapping
    @AuditAction(targetType = "ACTION", actionType = AuditActionType.CREATE)
    public ResponseEntity<ActionDTO> createAction() {
        log.info("Action Test created!!!");
        return ResponseEntity.ok(ActionDTO.builder()
                .id("01JKFK1J7B8F1KBZFHPR13J4D8")
                .code("TEST")
                .name("Test action")
                .status(AuthConstants.ModelStatus.ACTIVE)
                .build());
    }

}