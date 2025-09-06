package vn.vnsky.bcss.admin.config.auth;

import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import vn.vnsky.bcss.admin.constant.AuthConstants;

import java.util.List;

public class PartnerOAuth2UsernamePasswordAuthenticationConverter extends OAuth2UsernamePasswordAuthenticationConverter {

    @Override
    protected String getClientIdentity(MultiValueMap<String, String> parameters) {
        String clientIdentity = parameters.getFirst(CLIENT_IDENTITY);
        if (!StringUtils.hasText(clientIdentity) || parameters.get(CLIENT_IDENTITY).size() != 1) {
            CustomEndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, CLIENT_IDENTITY);
        }
        if (List.of(AuthConstants.VNSKY_CLIENT_ALIAS, AuthConstants.VNSKY_CLIENT_ID, AuthConstants.THIRD_PARTY_CLIENT_ID)
                .contains(clientIdentity)) {
            // forbidden client identity
            CustomEndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, CLIENT_IDENTITY);
        }
        return clientIdentity;
    }

}
