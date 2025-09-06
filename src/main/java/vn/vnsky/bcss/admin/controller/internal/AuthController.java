package vn.vnsky.bcss.admin.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.controller.AuthControllerBase;
import vn.vnsky.bcss.admin.dto.ForgotPasswordConfirmDTO;
import vn.vnsky.bcss.admin.dto.ForgotPasswordInitDTO;
import vn.vnsky.bcss.admin.dto.ModifyResultDTO;
import vn.vnsky.bcss.admin.service.DeviceService;
import vn.vnsky.bcss.admin.service.ObjectService;
import vn.vnsky.bcss.admin.service.UserService;

@Slf4j
@Tag(name = "Internal Authentication/Authorization API")
@RestController("internalAuthController")
@RequestMapping("${application.vnsky-web-oAuth2-client-info.api-prefix}/api/auth")
public class AuthController extends AuthControllerBase {

    @Autowired
    public AuthController(UserService userService, ObjectService objectService, DeviceService deviceService) {
        super(userService, objectService, deviceService);
    }

    @Operation(summary = "api quên mật khẩu (khởi tạo)")
    @PostMapping("/forgot-password/init")
    public ResponseEntity<ModifyResultDTO> forgotPasswordInit(@Validated(ForgotPasswordInitDTO.CaseInternal.class) @RequestBody ForgotPasswordInitDTO forgotPasswordInitDTO) {
        log.debug("REST request to init forgot password (Internal Site)");
        forgotPasswordInitDTO.setClientIdentity(AuthConstants.VNSKY_CLIENT_ALIAS);
        ModifyResultDTO modifyResultDTO = this.userService.forgotPasswordInit(forgotPasswordInitDTO);
        return ResponseEntity.status(HttpStatus.OK).body(modifyResultDTO);
    }

}
