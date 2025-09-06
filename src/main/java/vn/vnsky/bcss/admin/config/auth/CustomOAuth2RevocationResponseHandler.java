package vn.vnsky.bcss.admin.config.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import vn.vnsky.bcss.admin.service.UserService;
import vn.vnsky.bcss.admin.util.RequestUtil;

import java.security.Principal;

public class CustomOAuth2RevocationResponseHandler implements AuthenticationSuccessHandler {

    private static final String LOGOUT_ACTION_TYPE = "LOGOUT";

    private final OAuth2AuthorizationService oAuth2AuthorizationService;

    private final UserService userService;

    public CustomOAuth2RevocationResponseHandler(OAuth2AuthorizationService oAuth2AuthorizationService,
                                                 UserService userService) {
        this.oAuth2AuthorizationService = oAuth2AuthorizationService;
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        response.setStatus(HttpStatus.OK.value());
        if (authentication instanceof OAuth2TokenRevocationAuthenticationToken oAuth2TokenRevocationAuthenticationToken &&
            oAuth2TokenRevocationAuthenticationToken.getPrincipal() instanceof OAuth2ClientAuthenticationToken oAuth2ClientAuthenticationToken) {
            OAuth2Authorization oAuth2Authorization = this.oAuth2AuthorizationService.findByToken(oAuth2TokenRevocationAuthenticationToken.getToken(), OAuth2TokenType.REFRESH_TOKEN);
            if (oAuth2Authorization != null) {
                AbstractClientUserAuthenticationToken clientUserAuthenticationToken = oAuth2Authorization.getAttribute(Principal.class.getName());
                String clientIp = RequestUtil.getClientIP();
                this.userService.sendAccessLog(oAuth2ClientAuthenticationToken.getRegisteredClient(), clientUserAuthenticationToken, LOGOUT_ACTION_TYPE, clientIp);
            }
        }
    }

}
