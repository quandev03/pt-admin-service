package vn.vnsky.bcss.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.vnsky.bcss.admin.config.auth.*;
import vn.vnsky.bcss.admin.config.json.*;
import vn.vnsky.bcss.admin.dto.AppDTO;
import vn.vnsky.bcss.admin.service.ClientService;
import vn.vnsky.bcss.admin.service.ObjectService;
import vn.vnsky.bcss.admin.service.UserService;
import vn.vnsky.bcss.admin.tracing.RequestAuthInfoProvider;
import vn.vnsky.bcss.admin.util.SecurityUtil;

import java.sql.Timestamp;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true)
@Profile("default")
public class SecurityConfig {

    @Value("${spring.web-security.debug:true}")
    private boolean webSecurityDebug;

    private final CorsConfiguration configuration;

    @Autowired
    public SecurityConfig(CorsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(ApplicationProperties applicationProperties) {
        return web -> {
            WebSecurity.IgnoredRequestConfigurer webIgnoredRequestConfigurer = web
                    .debug(webSecurityDebug)
                    .ignoring()
                    .requestMatchers("/v3/api-docs")
                    .requestMatchers("/v3/api-docs/**")
                    .requestMatchers("/swagger-ui/**")
                    .requestMatchers("/actuator/**")
                    .requestMatchers(HttpMethod.POST, "/internal/api/clients");
            List<String> prefixes = List.of("", applicationProperties.getVnskyWebOAuth2ClientInfo().getApiPrefix(),
                    applicationProperties.getPartnerWebOAuth2ClientInfo().getApiPrefix(),
                    applicationProperties.getThirdPartyOAuth2ClientInfo().getApiPrefix());
            List<String> apiList = List.of("/oauth2/policy", "/oauth2/policy/**", "/api/auth/forgot-password/**", "/api/test/trace", "/api/test/message", "/api/users/search-by-object-code",
                    "/private/api/users/partner/internal/**");
            prefixes.forEach(prefix ->
                    apiList.forEach(apiPath ->
                            webIgnoredRequestConfigurer.requestMatchers(prefix + apiPath))
            )
            ;
        };
    }

    @Bean
    @Order(2)
    public SecurityFilterChain internalAuthorizationServerSecurityFilterChain(ApplicationProperties applicationProperties,
                                                                              HttpSecurity httpSecurity, JwtConfigHolder jwtConfigHolder,
                                                                              ObjectMapper objectMapper, MessageSource messageSource,
                                                                              OAuth2AuthorizationService oauth2AuthorizationService,
                                                                              ClientService clientService, UserService userService)
            throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(httpSecurity);
        OAuth2UsernamePasswordAuthenticationConverter oAuth2UsernamePasswordAuthenticationConverter = new InternalOAuth2UsernamePasswordAuthenticationConverter();
        AbstractOAuth2UsernamePasswordAuthenticationProvider oAuth2UsernamePasswordAuthenticationProvider =
                new InternalOAuth2UsernamePasswordAuthenticationProvider(applicationProperties, clientService, userService,
                        oauth2AuthorizationService, jwtConfigHolder.getJwtGenerator());
        oAuth2UsernamePasswordAuthenticationProvider.setMessageSource(messageSource);
        oAuth2UsernamePasswordAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        AuthenticationEntryPoint authenticationEntryPoint = new CustomAuthenticationEntryPoint(objectMapper, messageSource);
        AccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler(objectMapper, messageSource);
        RequestMatcher requestMatcher = new MediaTypeRequestMatcher(MediaType.ALL);

        GoogleAuthenticationConverter googleAuthenticationConverter = new GoogleAuthenticationConverter();
        GoogleAuthenticationProvider googleAuthenticationProvider = new GoogleAuthenticationProvider(
                applicationProperties, clientService, userService, oauth2AuthorizationService, jwtConfigHolder.getJwtGenerator());
        googleAuthenticationProvider.setMessageSource(messageSource);
        AuthorizationServerSettings authorizationServerSettings = this.createSettingsForPrefix(applicationProperties.getVnskyWebOAuth2ClientInfo().getApiPrefix());
        httpSecurity.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .authorizationServerSettings(authorizationServerSettings
                )
                .tokenRevocationEndpoint(oAuth2TokenRevocationEndpointConfigurer ->
                        oAuth2TokenRevocationEndpointConfigurer.revocationResponseHandler(new CustomOAuth2RevocationResponseHandler(oauth2AuthorizationService, userService)))
                .tokenEndpoint(tokenEndpoint ->
                        tokenEndpoint
                                .accessTokenRequestConverter(oAuth2UsernamePasswordAuthenticationConverter)
                                .accessTokenRequestConverter(googleAuthenticationConverter)
                                .authenticationProvider(oAuth2UsernamePasswordAuthenticationProvider)
                                .authenticationProvider(googleAuthenticationProvider)
                                .errorResponseHandler(new CustomOAuth2ErrorHandler(objectMapper, messageSource))
                )
                .oidc(Customizer.withDefaults())
        ;    // Enable OpenID Connect 1.0
        httpSecurity
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(authenticationEntryPoint, requestMatcher)
                        .defaultAccessDeniedHandlerFor(accessDeniedHandler, requestMatcher)
                )
        ;
        return httpSecurity.build();
    }


    @Bean
    @Order(3)
    public SecurityFilterChain partnerAuthorizationServerSecurityFilterChain(ApplicationProperties applicationProperties,
                                                                             HttpSecurity httpSecurity, JwtConfigHolder jwtConfigHolder,
                                                                             ObjectMapper objectMapper, MessageSource messageSource,
                                                                             OAuth2AuthorizationService oauth2AuthorizationService,
                                                                             ClientService clientService, UserService userService)
            throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(httpSecurity);
        OAuth2UsernamePasswordAuthenticationConverter oAuth2UsernamePasswordAuthenticationConverter = new PartnerOAuth2UsernamePasswordAuthenticationConverter();
        AbstractOAuth2UsernamePasswordAuthenticationProvider oAuth2UsernamePasswordAuthenticationProvider =
                new PartnerOAuth2UsernamePasswordAuthenticationProvider(applicationProperties, clientService, userService,
                        oauth2AuthorizationService, jwtConfigHolder.getJwtGenerator());
        oAuth2UsernamePasswordAuthenticationProvider.setMessageSource(messageSource);
        oAuth2UsernamePasswordAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        AuthenticationEntryPoint authenticationEntryPoint = new CustomAuthenticationEntryPoint(objectMapper, messageSource);
        AccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler(objectMapper, messageSource);
        RequestMatcher requestMatcher = new MediaTypeRequestMatcher(MediaType.ALL);
        AuthorizationServerSettings authorizationServerSettings = this.createSettingsForPrefix(applicationProperties.getPartnerWebOAuth2ClientInfo().getApiPrefix());
        httpSecurity.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .authorizationServerSettings(authorizationServerSettings
                )
                .tokenRevocationEndpoint(oAuth2TokenRevocationEndpointConfigurer ->
                        oAuth2TokenRevocationEndpointConfigurer.revocationResponseHandler(new CustomOAuth2RevocationResponseHandler(oauth2AuthorizationService, userService)))
                .tokenEndpoint(tokenEndpoint ->
                        tokenEndpoint
                                .accessTokenRequestConverter(oAuth2UsernamePasswordAuthenticationConverter)
                                .authenticationProvider(oAuth2UsernamePasswordAuthenticationProvider)
                                .errorResponseHandler(new CustomOAuth2ErrorHandler(objectMapper, messageSource))
                )
                .oidc(Customizer.withDefaults())
        ;    // Enable OpenID Connect 1.0
        httpSecurity
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(authenticationEntryPoint, requestMatcher)
                        .defaultAccessDeniedHandlerFor(accessDeniedHandler, requestMatcher)
                )
        ;
        return httpSecurity.build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain thirdPartyAuthorizationServerSecurityFilterChain(ApplicationProperties applicationProperties,
                                                                                HttpSecurity httpSecurity, JwtConfigHolder jwtConfigHolder,
                                                                                ObjectMapper objectMapper, MessageSource messageSource,
                                                                                OAuth2AuthorizationService oauth2AuthorizationService,
                                                                                ClientService clientService, UserService userService)
            throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(httpSecurity);
        OAuth2UsernamePasswordAuthenticationConverter oAuth2UsernamePasswordAuthenticationConverter = new ThirdPartyOAuth2UsernamePasswordAuthenticationConverter();
        AbstractOAuth2UsernamePasswordAuthenticationProvider oAuth2UsernamePasswordAuthenticationProvider =
                new ThirdPartyOAuth2UsernamePasswordAuthenticationProvider(applicationProperties, clientService, userService,
                        oauth2AuthorizationService, jwtConfigHolder.getJwtGenerator());
        oAuth2UsernamePasswordAuthenticationProvider.setMessageSource(messageSource);
        oAuth2UsernamePasswordAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        AuthenticationEntryPoint authenticationEntryPoint = new CustomAuthenticationEntryPoint(objectMapper, messageSource);
        AccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler(objectMapper, messageSource);
        RequestMatcher requestMatcher = new MediaTypeRequestMatcher(MediaType.ALL);

        AuthorizationServerSettings authorizationServerSettings = this.createSettingsForPrefix(applicationProperties.getThirdPartyOAuth2ClientInfo().getApiPrefix() + "/private");
        httpSecurity.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .authorizationServerSettings(authorizationServerSettings
                )
                .tokenRevocationEndpoint(oAuth2TokenRevocationEndpointConfigurer ->
                        oAuth2TokenRevocationEndpointConfigurer.revocationResponseHandler(new CustomOAuth2RevocationResponseHandler(oauth2AuthorizationService, userService)))
                .tokenEndpoint(tokenEndpoint ->
                        tokenEndpoint
                                .accessTokenRequestConverter(oAuth2UsernamePasswordAuthenticationConverter)
                                .authenticationProvider(oAuth2UsernamePasswordAuthenticationProvider)
                                .errorResponseHandler(new CustomOAuth2ErrorHandler(objectMapper, messageSource))
                )
                .oidc(Customizer.withDefaults())
        ;    // Enable OpenID Connect 1.0
        httpSecurity
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(authenticationEntryPoint, requestMatcher)
                        .defaultAccessDeniedHandlerFor(accessDeniedHandler, requestMatcher)
                )
        ;
        return httpSecurity.build();
    }

    @Bean
    @Order(6)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity httpSecurity, JwtConfigHolder jwtConfigHolder,
                                                          ObjectMapper objectMapper, MessageSource messageSource)
            throws Exception {
        AuthenticationEntryPoint authenticationEntryPoint = new CustomAuthenticationEntryPoint(objectMapper, messageSource);
        AccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler(objectMapper, messageSource);
        RequestMatcher requestMatcher = new MediaTypeRequestMatcher(MediaType.ALL);

        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/.well-known/**").permitAll()
                        .requestMatchers("/api/users/forgot-password/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Form login handles the redirect to the login page from the
                // authorization server filter chain
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(authenticationEntryPoint, requestMatcher)
                        .defaultAccessDeniedHandlerFor(accessDeniedHandler, requestMatcher)
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwtConfigurer -> jwtConfigurer
                                .decoder(jwtConfigHolder.getJwtDecoder())
                                .jwtAuthenticationConverter(jwtConfigHolder.getJwtAuthenticationConverter())
                        )
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
        ;
        return httpSecurity.build();
    }

    @Bean
    @Order(5)
    public SecurityFilterChain thirdPartyPublicAuthorizationServerSecurityFilterChain(ApplicationProperties applicationProperties,
                                                                                HttpSecurity httpSecurity, JwtConfigHolder jwtConfigHolder,
                                                                                ObjectMapper objectMapper, MessageSource messageSource,
                                                                                OAuth2AuthorizationService oauth2AuthorizationService,
                                                                                ClientService clientService, UserService userService)
            throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(httpSecurity);
        OAuth2UsernamePasswordAuthenticationConverter oAuth2UsernamePasswordAuthenticationConverter = new ThirdPartyOAuth2UsernamePasswordAuthenticationConverter();
        AbstractOAuth2UsernamePasswordAuthenticationProvider oAuth2UsernamePasswordAuthenticationProvider =
                new ThirdPartyOAuth2UsernamePasswordAuthenticationProvider(applicationProperties, clientService, userService,
                        oauth2AuthorizationService, jwtConfigHolder.getJwtGenerator());
        oAuth2UsernamePasswordAuthenticationProvider.setMessageSource(messageSource);
        oAuth2UsernamePasswordAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        AuthenticationEntryPoint authenticationEntryPoint = new CustomAuthenticationEntryPoint(objectMapper, messageSource);
        AccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler(objectMapper, messageSource);
        RequestMatcher requestMatcher = new MediaTypeRequestMatcher(MediaType.ALL);

        AuthorizationServerSettings authorizationServerSettings = this.createSettingsForPrefix(applicationProperties.getThirdPartyOAuth2ClientInfo().getApiPrefix() + "/public");
        httpSecurity.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .authorizationServerSettings(authorizationServerSettings
                )
                .tokenRevocationEndpoint(oAuth2TokenRevocationEndpointConfigurer ->
                        oAuth2TokenRevocationEndpointConfigurer.revocationResponseHandler(new CustomOAuth2RevocationResponseHandler(oauth2AuthorizationService, userService)))
                .tokenEndpoint(tokenEndpoint ->
                        tokenEndpoint
                                .accessTokenRequestConverter(oAuth2UsernamePasswordAuthenticationConverter)
                                .authenticationProvider(oAuth2UsernamePasswordAuthenticationProvider)
                                .errorResponseHandler(new CustomOAuth2ErrorHandler(objectMapper, messageSource))
                )
                .oidc(Customizer.withDefaults())
        ;    // Enable OpenID Connect 1.0
        httpSecurity
                // Redirect to the login page when not authenticated from the
                // authorization endpoint
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(authenticationEntryPoint, requestMatcher)
                        .defaultAccessDeniedHandlerFor(accessDeniedHandler, requestMatcher)
                )
        ;
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcOperations jdbcOperations) {
        return new JdbcRegisteredClientRepository(jdbcOperations);
    }

    @Bean
    public OAuth2AuthorizationService oAuth2AuthorizationService(JdbcOperations jdbcOperations,
                                                                 RegisteredClientRepository registeredClientRepository) {
        JdbcOAuth2AuthorizationService authorizationService = new JdbcOAuth2AuthorizationService(jdbcOperations, registeredClientRepository);
        JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper rowMapper = new JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(registeredClientRepository);
        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModules(new CoreJackson2Module());
        objectMapper.registerModules(SecurityJackson2Modules.getModules(classLoader));
        objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        // IMPORTANT: enable deserialize oauth2_authorization table attributes column
        objectMapper.addMixIn(AppDTO.class, AppMixin.class);
        objectMapper.addMixIn(OAuth2UsernamePasswordAuthenticationToken.class, OAuth2UsernamePasswordAuthenticationMixin.class);
        objectMapper.addMixIn(GoogleAuthenticationToken.class, GoogleAuthenticationMixin.class);
        objectMapper.addMixIn(Timestamp.class, TimestampMixin.class);
        objectMapper.addMixIn(Long.class, LongMixin.class);
        rowMapper.setObjectMapper(objectMapper);
        authorizationService.setAuthorizationRowMapper(rowMapper);
        return authorizationService;
    }

    @Bean
    public OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService(JdbcOperations jdbcOperations,
                                                                               RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, registeredClientRepository);
    }

    @Bean
    public JwtConfigHolder jwtConfigHolder(JwtProperties jwtProperties, UserService userService) {
        return new JwtConfigHolder(jwtProperties, userService);
    }

    @Bean
    public OAuth2TokenGenerator<Jwt> oauth2AccessTokenGenerator(JwtConfigHolder jwtConfigHolder) {
        return jwtConfigHolder.getJwtGenerator();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(JwtConfigHolder jwtConfigHolder) {
        return jwtConfigHolder.getJwkSource();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    private AuthorizationServerSettings createSettingsForPrefix(String prefix) {
        return AuthorizationServerSettings.builder()
                .authorizationEndpoint(prefix + "/oauth2/authorize")
                .deviceAuthorizationEndpoint(prefix + "/oauth2/device_authorization")
                .deviceVerificationEndpoint(prefix + "/oauth2/device_verification")
                .tokenEndpoint(prefix + "/oauth2/token")
                .jwkSetEndpoint(prefix + "/oauth2/jwks")
                .tokenRevocationEndpoint(prefix + "/oauth2/revoke")
                .tokenIntrospectionEndpoint(prefix + "/oauth2/introspect")
                .oidcClientRegistrationEndpoint(prefix + "/connect/register")
                .oidcUserInfoEndpoint(prefix + "/userinfo")
                .oidcLogoutEndpoint(prefix + "/connect/logout")
                .build();
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
    public AclAuthorizationInterceptor aclAuthorizationInterceptor(@Value("${spring.application.name}") String serviceCode,
                                                                   ApplicationProperties applicationProperties,
                                                                   ObjectService objectService) {
        return new AclAuthorizationInterceptor(serviceCode, applicationProperties, objectService);
    }

}