package vn.vnsky.bcss.admin.config.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import vn.vnsky.bcss.admin.constant.UserStatus;
import vn.vnsky.bcss.admin.dto.UserDTO;

@Getter
@Slf4j
public abstract class OAuth2UsernamePasswordAuthenticationConverter implements AuthenticationConverter {

    protected static final String CLIENT_IDENTITY = "client_identity";

    protected static final RequestMatcher USERNAME_PASSWORD_REQUEST_MATCHER = createUsernamePasswordRequestMatcher();

    private static RequestMatcher createUsernamePasswordRequestMatcher() {
        RequestMatcher postMethodMatcher = request -> "POST".equals(request.getMethod());
        RequestMatcher grantTypeParameterMatcher = request -> request.getParameter(OAuth2ParameterNames.GRANT_TYPE) != null;
        RequestMatcher usernameParameterMatcher = request -> request.getParameter(OAuth2ParameterNames.USERNAME) != null;
        RequestMatcher passwordTypeParameterMatcher = request -> request.getParameter(OAuth2ParameterNames.PASSWORD) != null;
        return new AndRequestMatcher(postMethodMatcher, grantTypeParameterMatcher,
                usernameParameterMatcher, passwordTypeParameterMatcher);
    }

    @Override
    public Authentication convert(HttpServletRequest request) {
        if (!USERNAME_PASSWORD_REQUEST_MATCHER.matches(request)) {
            return null;
        }
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            return null;
        }

        MultiValueMap<String, String> parameters = CustomEndpointUtils.getFormParameters(request);

        String grantType = parameters.getFirst(OAuth2ParameterNames.GRANT_TYPE);
        if (!StringUtils.hasText(grantType) || !CustomAuthenticationGrantTypes.PWD.getValue().equals(grantType)) {
            CustomEndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.GRANT_TYPE);
        }
        // username (REQUIRED)
        String username = parameters.getFirst(OAuth2ParameterNames.USERNAME);
        if (!StringUtils.hasText(username) || parameters.get(OAuth2ParameterNames.USERNAME).size() != 1) {
            CustomEndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.USERNAME);
        }
        // password (REQUIRED)
        String password = parameters.getFirst(OAuth2ParameterNames.PASSWORD);
        if (!StringUtils.hasText(password) || parameters.get(OAuth2ParameterNames.PASSWORD).size() != 1) {
            CustomEndpointUtils.throwError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.PASSWORD);
        }

        // clientIdentity (REQUIRED)
        String clientIdentity = this.getClientIdentity(parameters);
        UserDTO unauthenticatedUser = UserDTO.builder()
                .username(username)
                .status(UserStatus.INACTIVE.getValue())
                .build();
        return new OAuth2UsernamePasswordAuthenticationToken(unauthenticatedUser, password, clientIdentity);
    }

    protected abstract String getClientIdentity(MultiValueMap<String, String> parameters);

}
