package vn.vnsky.bcss.admin.config.auth;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.constant.ClientType;
import vn.vnsky.bcss.admin.service.ClientService;
import vn.vnsky.bcss.admin.service.UserService;

public class InternalOAuth2UsernamePasswordAuthenticationProvider extends AbstractOAuth2UsernamePasswordAuthenticationProvider {

    public InternalOAuth2UsernamePasswordAuthenticationProvider(ApplicationProperties applicationProperties,
                                                                ClientService clientService, UserService userService,
                                                                OAuth2AuthorizationService oAuth2AuthorizationService,
                                                                OAuth2TokenGenerator<? extends OAuth2Token> accessTokenGenerator) {
        super(applicationProperties, clientService, userService, oAuth2AuthorizationService, accessTokenGenerator);
    }

    @Override
    protected void validateOAuth2Client(AbstractClientUserAuthenticationToken authentication, RegisteredClient registeredClient) {
        if (!this.applicationProperties.getVnskyWebOAuth2ClientInfo().getClientId().equals(registeredClient.getClientId())) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                    String.format("Oauth2 client [%s] has submitted invalid client identity", registeredClient.getClientId()), OAUTH2_CLIENT_BASIC);
            throw new OAuth2AuthenticationException(error);
        }
    }

    @Override
    protected ClientType getClientType() {
        return ClientType.VNSKY;
    }

}
