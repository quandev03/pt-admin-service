package vn.vnsky.bcss.admin.config.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.constant.ErrorMessageConstant;
import vn.vnsky.bcss.admin.constant.UserLoginMethod;
import vn.vnsky.bcss.admin.dto.UserDTO;
import vn.vnsky.bcss.admin.service.ClientService;
import vn.vnsky.bcss.admin.service.UserService;

@Getter
@Setter
@Slf4j
public abstract class AbstractOAuth2UsernamePasswordAuthenticationProvider extends AbstractClientUserAuthenticationProvider
        implements AuthenticationProvider, MessageSourceAware {

    protected final ApplicationProperties applicationProperties;

    protected UserCache userCache = new NullUserCache();

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    protected CustomPreAuthenticationCheck preAuthenticationChecks = new CustomPreAuthenticationCheck();

    @Getter
    @Setter
    private PasswordEncoder passwordEncoder;

    protected AbstractOAuth2UsernamePasswordAuthenticationProvider(ApplicationProperties applicationProperties,
                                                                   ClientService clientService, UserService userService,
                                                                   OAuth2AuthorizationService oAuth2AuthorizationService,
                                                                   OAuth2TokenGenerator<? extends OAuth2Token> accessTokenGenerator) {
        super(clientService, userService, oAuth2AuthorizationService, accessTokenGenerator);
        this.applicationProperties = applicationProperties;
    }

    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  OAuth2UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        UserDTO userDTO = (UserDTO) userDetails;
        if (authentication.getCredentials() == null) {
            log.debug("Failed to authenticate since no credentials provided");
            OAuth2Error error = new OAuth2Error(ErrorMessageConstant.USERNAME_NOT_FOUND,
                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "password");
            throw new OAuth2AuthenticationException(error);
        }
        String presentedPassword = authentication.getCredentials();
        if (!this.passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            log.debug("Failed to authenticate since password does not match stored value");
            boolean locked = this.userService.lockUserForFailedLoginAttempts(userDTO);
            if (locked) {
                log.debug("Failed to authenticate since user account is locked");
                throw new LockedException(messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.locked", "User account is locked"));
            }
            OAuth2Error error = new OAuth2Error(ErrorMessageConstant.USERNAME_NOT_FOUND,
                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "password");
            throw new OAuth2AuthenticationException(error);
        } else {
            log.debug("Authenticate user success, reset failed login counter");
            this.userService.resetUserFailedLoginAttempts(userDTO);
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(OAuth2UsernamePasswordAuthenticationToken.class, authentication,
                () -> this.messages.getMessage("OAuth2UsernamePasswordAuthenticationProvider.onlySupports",
                        "Only OAuth2UsernamePasswordAuthenticationToken is supported"));
        String username = determineUsername(authentication);
        boolean cacheWasUsed = true;
        UserDetails user = this.userCache.getUserFromCache(username);
        if (user == null) {
            cacheWasUsed = false;
            user = this.retrieveUser(username, (OAuth2UsernamePasswordAuthenticationToken) authentication);
            Assert.notNull(user, "retrieveUser returned null - a violation of the interface contract");
        }
        try {
            this.preAuthenticationChecks.check(user);
            this.additionalAuthenticationChecks(user, (OAuth2UsernamePasswordAuthenticationToken) authentication);
        } catch (AuthenticationException ex) {
            if (!cacheWasUsed) {
                throw ex;
            }
            // There was a problem, so try again after checking
            // we're using latest data (i.e. not from the cache)
            cacheWasUsed = false;
            user = retrieveUser(username, (OAuth2UsernamePasswordAuthenticationToken) authentication);
            this.preAuthenticationChecks.check(user);
            this.additionalAuthenticationChecks(user, (OAuth2UsernamePasswordAuthenticationToken) authentication);
        }
        if (!cacheWasUsed) {
            this.userCache.putUserInCache(user);
        }
        return this.createSuccessAuthentication(authentication, user);
    }

    private String determineUsername(Authentication authentication) {
        return (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
    }

    @Override
    public void setMessageSource(@NonNull MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
        this.preAuthenticationChecks.setMessages(this.messages);
        this.preAuthenticationChecks.setMessages(this.messages);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OAuth2UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    protected String getLoginType() {
        return UserLoginMethod.LOGIN_USERNAME.name();
    }

}
