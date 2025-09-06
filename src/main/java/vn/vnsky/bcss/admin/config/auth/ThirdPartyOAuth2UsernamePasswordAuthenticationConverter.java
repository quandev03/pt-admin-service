package vn.vnsky.bcss.admin.config.auth;

import org.springframework.util.MultiValueMap;
import vn.vnsky.bcss.admin.constant.AuthConstants;

public class ThirdPartyOAuth2UsernamePasswordAuthenticationConverter extends OAuth2UsernamePasswordAuthenticationConverter {

    @Override
    protected String getClientIdentity(MultiValueMap<String, String> parameters) {
        return AuthConstants.THIRD_PARTY_CLIENT_ID;
    }

}
