package vn.vnsky.bcss.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.vnsky.bcss.admin.constant.ClientType;
import vn.vnsky.bcss.admin.dto.ClientDTO;
import vn.vnsky.bcss.admin.service.ClientService;

@Slf4j
public abstract class ClientControllerBase {

    protected final ClientService clientService;

    protected ClientControllerBase(ClientService clientService) {
        this.clientService = clientService;
    }

    @Operation(summary = "api chi tiết đối tác")
    @GetMapping("/{identity}")
    public ResponseEntity<ClientDTO> detail(@PathVariable(name = "identity") String identity) {
        log.debug("REST request to get client (Internal Site)");
        ClientDTO clientDTO;
        if(identity.startsWith("3RD")) {
            clientDTO = this.clientService.detail(identity, ClientType.THIRD_PARTY);
        }
        else {
            clientDTO = this.clientService.detail(identity, ClientType.PARTNER_AND_VNSKY);
        }
        return ResponseEntity.ok(clientDTO);
    }
}
