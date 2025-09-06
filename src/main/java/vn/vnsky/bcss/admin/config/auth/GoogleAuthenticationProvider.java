package vn.vnsky.bcss.admin.config.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.constant.ClientType;
import vn.vnsky.bcss.admin.constant.ErrorMessageConstant;
import vn.vnsky.bcss.admin.constant.UserLoginMethod;
import vn.vnsky.bcss.admin.dto.AppDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;
import vn.vnsky.bcss.admin.service.ClientService;
import vn.vnsky.bcss.admin.service.UserService;

import java.io.StringReader;

@Getter
@Setter
@Slf4j
public class GoogleAuthenticationProvider extends AbstractClientUserAuthenticationProvider
        implements AuthenticationProvider, MessageSourceAware {

    private static final String OAUTH2_CLIENT_BASIC = "oauth2_client_basic";

    protected final ApplicationProperties applicationProperties;

    protected final GoogleClientSecrets clientSecrets;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    protected CustomPreAuthenticationCheck preAuthenticationChecks = new CustomPreAuthenticationCheck();

    @SneakyThrows
    public GoogleAuthenticationProvider(ApplicationProperties applicationProperties,
                                        ClientService clientService, UserService userService,
                                        OAuth2AuthorizationService oAuth2AuthorizationService,
                                        OAuth2TokenGenerator<? extends OAuth2Token> accessTokenGenerator) {
        super(clientService, userService, oAuth2AuthorizationService, accessTokenGenerator);
        this.clientSecrets =
                GoogleClientSecrets.load(
                        JacksonFactory.getDefaultInstance(), new StringReader(applicationProperties.getGoogleCloudApiKey()));
        this.applicationProperties = applicationProperties;
    }

    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        GoogleAuthenticationToken googleAuthentication = (GoogleAuthenticationToken) authentication;

        Authentication existedAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if (existedAuthentication instanceof OAuth2ClientAuthenticationToken oAuth2ClientAuthenticationToken) {
            RegisteredClient registeredClient = oAuth2ClientAuthenticationToken.getRegisteredClient();
            if (registeredClient == null) {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                        "Unauthorized oauth2 client.", OAUTH2_CLIENT_BASIC);
                throw new OAuth2AuthenticationException(error);
            }
            googleAuthentication.setDetails(registeredClient);
            log.info("Retrieved registered client {} - {}", registeredClient.getClientId(), registeredClient.getClientName());
            String authCode = googleAuthentication.getCredentials();
            String origin = googleAuthentication.getOrigin();
            try {
                // Exchange auth code for access token
                GoogleTokenResponse tokenResponse =
                        new GoogleAuthorizationCodeTokenRequest(
                                new NetHttpTransport(),
                                JacksonFactory.getDefaultInstance(),
                                "https://oauth2.googleapis.com/token",
                                clientSecrets.getDetails().getClientId(),
                                clientSecrets.getDetails().getClientSecret(),
                                authCode,
                                origin)  // Specify the same redirect URI that you use with your web
                                // app.
                                // If you don't have a web version of your app, you can
                                // specify an empty string.
                                .execute();

                // Get profile info from ID token
                GoogleIdToken idToken = tokenResponse.parseIdToken();
                GoogleIdToken.Payload payload = idToken.getPayload();
                String userId = payload.getSubject();  // Use this value as a key to identify a user.
                String email = payload.getEmail();
                boolean emailVerified = payload.getEmailVerified();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");
                String locale = (String) payload.get("locale");
                String familyName = (String) payload.get("family_name");
                String givenName = (String) payload.get("given_name");

                UserDTO userDTO = this.retrieveUser(email, googleAuthentication);
                if (UserLoginMethod.LOGIN_GOOGLE.getValue() != userDTO.getLoginMethod()) {
                    OAuth2Error error = new OAuth2Error(ErrorMessageConstant.INVALID_LOGIN_METHOD,
                            "Người dùng không được phép đăng nhập qua Google.", null);
                    throw new OAuth2AuthenticationException(error);
                }
                userDTO.getAttributes().put("googleUserId", userId);
                userDTO.getAttributes().put("googleEmailVerified", emailVerified);
                userDTO.getAttributes().put("googleUserFullname", name);
                userDTO.getAttributes().put("googleAvatarUrl", pictureUrl);
                userDTO.getAttributes().put("googleLocale", locale);
                userDTO.getAttributes().put("googleFamilyName", familyName);
                userDTO.getAttributes().put("googleGivenName", givenName);
                userDTO.getAttributes().put("appId", registeredClient.getId());
                userDTO.getAttributes().put("appCode", registeredClient.getClientId());
                userDTO.getAttributes().put(AppDTO.Fields.ssoProvider, "GOOGLE");

                this.preAuthenticationChecks.check(userDTO);
                return this.createSuccessAuthentication(authentication, userDTO);
            } catch (HttpResponseException ex) {
                OAuth2Error error = new OAuth2Error(ErrorMessageConstant.SYSTEM_ERROR,
                        "Google token exchange exception: " + ex.getMessage(), null);
                throw new OAuth2AuthenticationException(error);
            }

        } else {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                    "Unauthorized OAuth2 client.", OAUTH2_CLIENT_BASIC);
            throw new OAuth2AuthenticationException(error);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return GoogleAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public void setMessageSource(@NonNull MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
        this.preAuthenticationChecks.setMessages(this.messages);
    }

    @Override
    protected void validateOAuth2Client(AbstractClientUserAuthenticationToken authentication, RegisteredClient registeredClient) {
        if (!this.applicationProperties.getVnskyWebOAuth2ClientInfo().getClientId().equals(registeredClient.getClientId())) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                    String.format("Oauth2 client partner [%s] cannot login via Google SSO", registeredClient.getClientId()), OAUTH2_CLIENT_BASIC);
            throw new OAuth2AuthenticationException(error);
        }
    }

    @Override
    protected String getLoginType() {
        return UserLoginMethod.LOGIN_GOOGLE.name();
    }

    @Override
    protected ClientType getClientType() {
        return ClientType.VNSKY;
    }
}
