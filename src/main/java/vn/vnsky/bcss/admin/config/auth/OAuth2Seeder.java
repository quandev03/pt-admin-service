package vn.vnsky.bcss.admin.config.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.config.JwtProperties;

@Slf4j
@Component
public class OAuth2Seeder {

    private final ApplicationProperties applicationProperties;

    private final JwtProperties jwtProperties;

    private final RegisteredClientRepository registeredClientRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public OAuth2Seeder(ApplicationProperties applicationProperties, JwtProperties jwtProperties,
                        RegisteredClientRepository registeredClientRepository, PasswordEncoder passwordEncoder) {
        this.applicationProperties = applicationProperties;
        this.jwtProperties = jwtProperties;
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener
    public void appReady(ApplicationReadyEvent event) {
        log.info("[APP_SEEDER] App running up after {}", event.getTimeTaken());
        this.createOAuth2ClientIfNotExists(this.applicationProperties.getVnskyWebOAuth2ClientInfo());
        this.createOAuth2ClientIfNotExists(this.applicationProperties.getPartnerWebOAuth2ClientInfo());
        this.createOAuth2ClientIfNotExists(this.applicationProperties.getThirdPartyOAuth2ClientInfo());
        this.createOAuth2ClientIfNotExists(this.applicationProperties.getSaleAppOAuth2ClientInfo());
    }

    private void createOAuth2ClientIfNotExists(ApplicationProperties.OAuth2WebClientInfo oAuth2WebClientInfo) {
        if (this.registeredClientRepository.findByClientId(oAuth2WebClientInfo.getClientId()) == null) {
            RegisteredClient registeredClient = RegisteredClient
                    .withId(oAuth2WebClientInfo.getId())
                    .clientId(oAuth2WebClientInfo.getClientId())
                    .clientName(oAuth2WebClientInfo.getClientName())
                    .clientSecret(this.passwordEncoder.encode(oAuth2WebClientInfo.getClientSecret()))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(CustomAuthenticationGrantTypes.PWD)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .redirectUri("http://127.0.0.1:8080/login/oauth2/code/oidc-client")
                    .postLogoutRedirectUri("http://127.0.0.1:8080/")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .scope(OidcScopes.ADDRESS)
                    .scope(OidcScopes.EMAIL)
                    .scope(OidcScopes.PHONE)
                    .clientSettings(ClientSettings.builder()
                            .requireAuthorizationConsent(true)
                            .setting("allowedApiPrefixes", new String[] { "/login/**" })
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .idTokenSignatureAlgorithm(SignatureAlgorithm.valueOf(this.jwtProperties.getAlgorithm()))
                            .accessTokenTimeToLive(oAuth2WebClientInfo.getDefaultAccessTokenValidity())
                            .refreshTokenTimeToLive(oAuth2WebClientInfo.getDefaultRefreshTokenValidity())
                            .reuseRefreshTokens(oAuth2WebClientInfo.isReuseRefreshToken())
                            .build())
                    .build();
            this.registeredClientRepository.save(registeredClient);
            log.info("[APP_SEEDER] Inserted oauth2 client: {}", oAuth2WebClientInfo);
        }
    }

}
