package vn.vnsky.bcss.admin.config.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import vn.vnsky.bcss.admin.dto.AppDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;

import java.util.Collection;

public abstract class AbstractClientUserAuthenticationToken extends AbstractAuthenticationToken {

    protected AbstractClientUserAuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    protected void makeAuthenticated(UserDetails userDetails) {
        setAuthenticated(true);
        Object details = this.getDetails();
        if (details instanceof RegisteredClient registeredClient) {
            AppDTO appDTO = AppDTO.builder()
                    .id(registeredClient.getId())
                    .code(registeredClient.getClientId())
                    .name(registeredClient.getClientName())
                    .build();
            if (userDetails instanceof UserDTO userDTO) {
                Object ssoProviderObj = userDTO.getAttributes().get(AppDTO.Fields.ssoProvider);
                if (ssoProviderObj instanceof String ssoProvider) {
                    appDTO.setSsoProvider(ssoProvider);
                }
            }
            this.setDetails(appDTO);
        }
    }

    protected abstract String getClientIdentity();

}
