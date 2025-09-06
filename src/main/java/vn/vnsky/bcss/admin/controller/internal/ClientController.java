package vn.vnsky.bcss.admin.controller.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.vnsky.bcss.admin.constant.ClientType;
import vn.vnsky.bcss.admin.controller.ClientControllerBase;
import vn.vnsky.bcss.admin.dto.ChangeStatusDTO;
import vn.vnsky.bcss.admin.dto.ClientDTO;
import vn.vnsky.bcss.admin.service.ClientService;

@Slf4j
@Tag(name = "Internal Client API")
@RestController("internalClientController")
@RequestMapping("${application.vnsky-web-oAuth2-client-info.api-prefix}/api/clients")
public class ClientController extends ClientControllerBase {

    @Autowired
    public ClientController(ClientService clientService) {
        super(clientService);
    }

    @Operation(summary = "api tìm kiếm đối tác")
    @GetMapping
    public ResponseEntity<Page<ClientDTO>> search(@RequestParam(name = "q", required = false) String term,
                                                  @RequestParam(name = "includeVnsky", required = false, defaultValue = "true") Boolean includeVnsky,
                                                  Pageable pageable) {
        log.debug("REST request to search client (Internal Site)");
        Page<ClientDTO> clientDTOPage = this.clientService.search(term, pageable,
                Boolean.TRUE.equals(includeVnsky) ? ClientType.PARTNER_AND_VNSKY : ClientType.PARTNER);
        return ResponseEntity.ok(clientDTOPage);
    }

    @Operation(summary = "api cập nhật thông tin đối tác")
    @PatchMapping("/{identity}")
    public ResponseEntity<ClientDTO> updateInfo(@PathVariable(name = "identity") String identity, @Validated @RequestBody ClientDTO clientDetailDTO) {
        log.debug("REST request to update client info (Internal Site)");
        ClientDTO createdClientDetailDTO = this.clientService.patch(identity, clientDetailDTO, ClientType.PARTNER);
        return ResponseEntity.ok(createdClientDetailDTO);
    }

    @Operation(summary = "api thay đổi trạng thái đối tác")
    @PutMapping("/{identity}/status")
    public ResponseEntity<ClientDTO> changeStatus(@PathVariable(name = "identity") String identity, @Validated @RequestBody ChangeStatusDTO changeStatusDTO) {
        log.debug("REST request to update client status (Internal Site)");
        ClientDTO createdClientDetailDTO = this.clientService.changeStatus(identity, changeStatusDTO, ClientType.PARTNER);
        return ResponseEntity.ok(createdClientDetailDTO);
    }

}
