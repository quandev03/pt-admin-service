package vn.vnsky.bcss.admin.controller.noauth;

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
import vn.vnsky.bcss.admin.constant.ClientType;
import vn.vnsky.bcss.admin.controller.ClientControllerBase;
import vn.vnsky.bcss.admin.dto.ClientDTO;
import vn.vnsky.bcss.admin.service.ClientService;

@Slf4j
@Tag(name = "Noauth Client API")
@RestController("noauthClientController")
@RequestMapping("/internal/api/clients")
public class ClientController extends ClientControllerBase {

    @Autowired
    public ClientController(ClientService clientService) {
        super(clientService);
    }

    @Operation(summary = "api tạo đối tác")
    @PostMapping
    public ResponseEntity<ClientDTO> create(@RequestBody @Validated ClientDTO clientDTO) {
        log.debug("REST request to create client (Internal Site)");
        ClientDTO newClientDTO = this.clientService.create(clientDTO, ClientType.PARTNER);
        return ResponseEntity.status(HttpStatus.CREATED).body(newClientDTO);
    }

}
