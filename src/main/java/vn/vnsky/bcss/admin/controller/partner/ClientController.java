package vn.vnsky.bcss.admin.controller.partner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.vnsky.bcss.admin.constant.ClientType;
import vn.vnsky.bcss.admin.controller.ClientControllerBase;
import vn.vnsky.bcss.admin.dto.ClientDTO;
import vn.vnsky.bcss.admin.service.ClientService;
import vn.vnsky.bcss.admin.util.SecurityUtil;

@Slf4j
@Tag(name = "Partner Client API")
@RestController("partnerClientController")
@RequestMapping("${application.partner-web-oAuth2-client-info.api-prefix}/api/clients")
public class ClientController extends ClientControllerBase {

    @Autowired
    public ClientController(ClientService clientService) {
        super(clientService);
    }

    @Operation(summary = "api lấy thông tin đối tác hiện tại")
    @GetMapping("/profile")
    public ResponseEntity<ClientDTO> detail() {
        log.debug("REST request to get current client profile (Partner Site)");
        String clientId = SecurityUtil.getCurrentClientId();
        ClientDTO clientDTO = this.clientService.detail(clientId, ClientType.THIRD_PARTY);
        return ResponseEntity.ok(clientDTO);
    }

    @Operation(summary = "api cập nhật thông tin đối tác hiện tại")
    @PatchMapping("/profile")
    public ResponseEntity<ClientDTO> update(@Validated @RequestBody ClientDTO clientDetailDTO) {
        log.debug("REST request to update current client profile (Partner Site)");
        String clientId = SecurityUtil.getCurrentClientId();
        ClientDTO createdClientDetailDTO = this.clientService.patch(clientId, clientDetailDTO, ClientType.THIRD_PARTY);
        return ResponseEntity.ok(createdClientDetailDTO);
    }

}
