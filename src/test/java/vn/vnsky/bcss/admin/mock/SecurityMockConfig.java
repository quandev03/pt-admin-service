package vn.vnsky.bcss.admin.mock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.config.auth.AclAuthorizationInterceptor;
import vn.vnsky.bcss.admin.config.auth.CustomAuthenticationGrantTypes;
import vn.vnsky.bcss.admin.service.ObjectService;
import vn.vnsky.bcss.admin.tracing.RequestAuthInfoProvider;
import vn.vnsky.bcss.admin.util.SecurityUtil;

@Configuration
@Profile("test")
public class SecurityMockConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(request -> request.anyRequest()
                        .permitAll())
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        return new RegisteredClientRepository() {

            private final InMemoryRegisteredClientRepository inMemoryRegisteredClientRepository = new InMemoryRegisteredClientRepository(
                    RegisteredClient
                            .withId("5ae83f03-ca04-453a-a5ed-ef1c9afc361c")
                            .clientId("test-client")
                            .clientName("Test Client")
                            .clientSecret(null)
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
                                    .setting("allowedApiPrefixes", new String[]{"/login/**"})
                                    .build())
                            .build()
            );

            @Override
            public void save(RegisteredClient registeredClient) {
                this.inMemoryRegisteredClientRepository.save(registeredClient);
            }

            @Override
            public RegisteredClient findById(String id) {
                return this.inMemoryRegisteredClientRepository.findById(id);
            }

            @Override
            public RegisteredClient findByClientId(String clientId) {
                return this.inMemoryRegisteredClientRepository.findByClientId(clientId);
            }

        };
    }

    @Bean
    public RequestAuthInfoProvider requestAuthInfoProvider() {
        return authInfo -> SecurityUtil.getCurrentUser()
                .ifPresent(userDTO -> {
                    authInfo.put("CLIENT_ID", userDTO.getClient().getId());
                    authInfo.put("CLIENT_CODE", userDTO.getClient().getCode());
                    authInfo.put("CLIENT_NAME", userDTO.getClient().getName());
                    authInfo.put("SITE_ID", userDTO.getAttributes().get("appId"));
                    authInfo.put("SITE_CODE", userDTO.getAttributes().get("appCode"));
                    authInfo.put("SITE_NAME", userDTO.getAttributes().get("appName"));
                    authInfo.put("USER_ID", userDTO.getId());
                    authInfo.put("USER_USERNAME", userDTO.getUsername());
                    authInfo.put("USER_FULLNAME", userDTO.getFullname());
                    authInfo.put("USER_PREFERRED_USERNAME", userDTO.getAttributes().get("preferredUsername"));
                });
    }

    @Bean
    @ConditionalOnMissingBean(AclAuthorizationInterceptor.class)
    public AclAuthorizationInterceptor aclAuthorizationInterceptor(@Value("${spring.application.name}") String serviceCode, ApplicationProperties applicationProperties,
                                                                   ObjectService objectService) {
        return new AclAuthorizationInterceptor(serviceCode, applicationProperties, objectService);
    }

}
