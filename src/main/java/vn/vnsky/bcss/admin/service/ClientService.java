package vn.vnsky.bcss.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.vnsky.bcss.admin.constant.ClientType;
import vn.vnsky.bcss.admin.dto.ChangeStatusDTO;
import vn.vnsky.bcss.admin.dto.ClientDTO;

public interface ClientService {

    ClientDTO loadClientByIdentityOrAlias(String clientIdentity, ClientType clientType);

    Page<ClientDTO> search(String term, Pageable pageable, ClientType clientType);

    ClientDTO create(ClientDTO clientDTO, ClientType clientType);

    ClientDTO patch(String identity, ClientDTO clientDTO, ClientType clientType);

    ClientDTO changeStatus(String identity, ChangeStatusDTO clientDTO, ClientType clientType);

    ClientDTO detail(String identity, ClientType clientType);

}
