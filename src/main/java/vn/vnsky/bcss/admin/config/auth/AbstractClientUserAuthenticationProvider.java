package vn.vnsky.bcss.admin.config.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.constant.ClientType;
import vn.vnsky.bcss.admin.constant.ErrorMessageConstant;
import vn.vnsky.bcss.admin.dto.ClientDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;
import vn.vnsky.bcss.admin.service.ClientService;
import vn.vnsky.bcss.admin.service.UserService;
import vn.vnsky.bcss.admin.util.RequestUtil;
import vn.vnsky.bcss.admin.util.SecurityUtil;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Slf4j
public abstract class AbstractClientUserAuthenticationProvider {

    protected static final String OAUTH2_CLIENT_BASIC = "oauth2_client_basic";

    protected final ClientService clientService;

    protected final UserService userService;

    protected final OAuth2AuthorizationService oAuth2AuthorizationService;

    protected final OAuth2TokenGenerator<? extends OAuth2Token> accessTokenGenerator;

    protected final OAuth2TokenGenerator<? extends OAuth2Token> refreshTokenGenerator;

    protected AbstractClientUserAuthenticationProvider(ClientService clientService, UserService userService,
                                                       OAuth2AuthorizationService oAuth2AuthorizationService,
                                                       OAuth2TokenGenerator<? extends OAuth2Token> accessTokenGenerator) {
        this.oAuth2AuthorizationService = oAuth2AuthorizationService;
        this.clientService = clientService;
        this.userService = userService;
        this.accessTokenGenerator = accessTokenGenerator;
        this.refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
    }

    protected UserDTO retrieveUser(String username, AbstractClientUserAuthenticationToken authentication)
            throws AuthenticationException {
        try {
            log.debug("Authenticating {}", username);
            Authentication existedAuthentication = SecurityContextHolder.getContext().getAuthentication();
            if (existedAuthentication instanceof OAuth2ClientAuthenticationToken oAuth2ClientAuthenticationToken) {
                if (oAuth2ClientAuthenticationToken.getRegisteredClient() == null) {
                    OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                            "Unauthorized oauth2 client.", OAUTH2_CLIENT_BASIC);
                    throw new OAuth2AuthenticationException(error);
                }
                RegisteredClient registeredClient = oAuth2ClientAuthenticationToken.getRegisteredClient();
                this.validateOAuth2Client(authentication, registeredClient);
                authentication.setDetails(registeredClient);
                ClientDTO clientDTO = this.clientService.loadClientByIdentityOrAlias(
                        authentication.getClientIdentity(), this.getClientType()
                );
                if (clientDTO == null) {
                    OAuth2Error error = new OAuth2Error("invalid_client_identity",
                            "AbstractUserDetailsAuthenticationProvider.clientIdentityNotFound", "client_identity");
                    throw new OAuth2AuthenticationException(error);
                }
                if (AuthConstants.ModelStatus.ACTIVE != clientDTO.getStatus()) {
                    OAuth2Error error = new OAuth2Error("invalid_client_identity",
                            "AbstractUserDetailsAuthenticationProvider.clientNotEnabled", "client_identity");
                    throw new OAuth2AuthenticationException(error);
                }
                UserDTO userDTO = this.userService.loadUserByClientAndUser(clientDTO.getId(), username);
                if (userDTO == null) {
                    OAuth2Error error = new OAuth2Error(ErrorMessageConstant.USERNAME_NOT_FOUND,
                            "AbstractUserDetailsAuthenticationProvider.userNotFound", "password");
                    throw new OAuth2AuthenticationException(error);
                }
                userDTO.getAttributes().put("appId", registeredClient.getId());
                userDTO.getAttributes().put("appCode", registeredClient.getClientId());
                userDTO.getAttributes().put("appName", registeredClient.getClientName());
                userDTO.getAttributes().put("preferred_username", SecurityUtil.getPreferredUsername(userDTO));
                return userDTO;
            } else {
                OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                        "Unauthorized OAuth2 client.", OAUTH2_CLIENT_BASIC);
                throw new OAuth2AuthenticationException(error);
            }

        } catch (InternalAuthenticationServiceException ex) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                    "Internal authentication exception: " + ex.getMessage(), null);
            throw new OAuth2AuthenticationException(error);
        } catch (OAuth2AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            OAuth2Error error = new OAuth2Error(ErrorMessageConstant.SYSTEM_ERROR,
                    "System exception: " + ex.getMessage(), null);
            throw new OAuth2AuthenticationException(error);
        }
    }

    protected final Authentication createSuccessAuthentication(Authentication authentication,
                                                         UserDetails userDetails) {
        log.info("Authenticated partner user, {}", userDetails);
        AbstractClientUserAuthenticationToken clientUserAuthenticationToken = (AbstractClientUserAuthenticationToken) authentication;
        RegisteredClient registeredClient = (RegisteredClient) clientUserAuthenticationToken.getDetails();
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
        }
        clientUserAuthenticationToken.makeAuthenticated(userDetails);
        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(((UserDTO) userDetails).getId())
                .authorizationGrantType(CustomAuthenticationGrantTypes.PWD);
        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(clientUserAuthenticationToken)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorization(authorizationBuilder.build())
                .authorizationGrantType(CustomAuthenticationGrantTypes.PWD)
                .authorizationGrant(clientUserAuthenticationToken);

        // ----- Access token -----
        OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();
        OAuth2Token generatedAccessToken = this.accessTokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the access token.", null);
            throw new OAuth2AuthenticationException(error);
        }

        if (log.isTraceEnabled()) {
            log.trace("Generated access token");
        }

        OAuth2AccessToken accessToken = this.accessToken(authorizationBuilder,
                generatedAccessToken, tokenContext);

        // ----- Refresh token -----
        OAuth2RefreshToken refreshToken = null;
        // Do not issue refresh token to public client
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
            tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
            OAuth2Token generatedRefreshToken = this.refreshTokenGenerator.generate(tokenContext);
            if (generatedRefreshToken != null) {
                if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
                    OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
                            "The token generator failed to generate a valid refresh token.", null);
                    throw new OAuth2AuthenticationException(error);
                }

                if (log.isTraceEnabled()) {
                    log.trace("Generated refresh token");
                }

                refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
                OAuth2Authorization refreshOAuth2Authorization = authorizationBuilder
                        .attribute(Principal.class.getName(), clientUserAuthenticationToken)
                        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                        .refreshToken(refreshToken)
                        .build();
                this.oAuth2AuthorizationService.save(refreshOAuth2Authorization);
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("Saved authorization");
        }

        Map<String, Object> additionalParameters = Collections.emptyMap();

        if (log.isTraceEnabled()) {
            log.trace("Authenticated token request");
        }
        String clientIp = RequestUtil.getClientIP();
        this.userService.sendAccessLog(registeredClient, clientUserAuthenticationToken, this.getLoginType(), clientIp);
        return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientUserAuthenticationToken, accessToken, refreshToken,
                additionalParameters);
    }

    private <T extends OAuth2Token> OAuth2AccessToken accessToken(OAuth2Authorization.Builder builder, T token,
                                                                  OAuth2TokenContext accessTokenContext) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, token.getTokenValue(),
                token.getIssuedAt(), token.getExpiresAt(), accessTokenContext.getAuthorizedScopes());
        OAuth2TokenFormat accessTokenFormat = accessTokenContext.getRegisteredClient()
                .getTokenSettings()
                .getAccessTokenFormat();
        builder.token(accessToken, metadata -> {
            if (token instanceof ClaimAccessor claimAccessor) {
                metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, claimAccessor.getClaims());
            }
            metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, false);
            metadata.put(OAuth2TokenFormat.class.getName(), accessTokenFormat.getValue());
        });

        return accessToken;
    }

    protected abstract void validateOAuth2Client(AbstractClientUserAuthenticationToken authentication, RegisteredClient registeredClient);

    protected abstract String getLoginType();

    protected abstract ClientType getClientType();

}
