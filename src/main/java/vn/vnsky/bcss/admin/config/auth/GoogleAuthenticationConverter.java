package vn.vnsky.bcss.admin.config.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

@Component
public class GoogleAuthenticationConverter implements AuthenticationConverter {

    private static final String SSO_PROVIDER = "sso_provider";

    private static final String ORIGIN = "origin";

    private static final String GOOGLE_SSO_PROVIDER = "google";

    @Nullable
    @Override
    public Authentication convert(HttpServletRequest request) {
        MultiValueMap<String, String> parameters = CustomEndpointUtils.getFormParameters(request);

        // grant_type (REQUIRED)
        String grantType = parameters.getFirst(OAuth2ParameterNames.GRANT_TYPE);
        if (!CustomAuthenticationGrantTypes.SSO.getValue().equals(grantType)) {
            return null;
        }

        // sso_provider (REQUIRED)
        String ssoProvider = parameters.getFirst(SSO_PROVIDER);
        if (!GOOGLE_SSO_PROVIDER.equals(ssoProvider)) {
            return null;
        }

        // code (REQUIRED)
        String code = parameters.getFirst(OAuth2ParameterNames.CODE);
        if (!StringUtils.hasText(code) || parameters.get(OAuth2ParameterNames.CODE).size() != 1) {
            CustomEndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.CODE,
                    CustomEndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
        }

        // js web origin (REQUIRED)
        String origin = parameters.getFirst(ORIGIN);
        if (!StringUtils.hasText(origin) || parameters.get(ORIGIN).size() != 1) {
            CustomEndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, ORIGIN,
                    CustomEndpointUtils.ACCESS_TOKEN_REQUEST_ERROR_URI);
        }
        return new GoogleAuthenticationToken(code, origin);
    }
}
