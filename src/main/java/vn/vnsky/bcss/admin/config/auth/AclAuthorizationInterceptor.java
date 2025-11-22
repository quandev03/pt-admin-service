package vn.vnsky.bcss.admin.config.auth;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import vn.vnsky.bcss.admin.config.ApplicationProperties;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.dto.PolicyCheckDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;
import vn.vnsky.bcss.admin.service.ObjectService;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class AclAuthorizationInterceptor implements HandlerInterceptor {

    private static final String WILD_CARD_URI = "/**";

    private final String serviceCode;

    private final ObjectService objectService;

    private final Map<String, RequestMatcher> oauth2ClientAclRule;

    public AclAuthorizationInterceptor(String serviceCode, ApplicationProperties applicationProperties,
                                       ObjectService objectService) {
        this.serviceCode = serviceCode;
        this.objectService = objectService;
        Map<String, RequestMatcher> oauth2ClientAclRuleTmp = new HashMap<>();
        RequestMatcher internalApiUriMatcher = new AntPathRequestMatcher(applicationProperties.getVnskyWebOAuth2ClientInfo().getApiPrefix() + WILD_CARD_URI);
        RequestMatcher partnerApiUriMatcher = new AntPathRequestMatcher( applicationProperties.getPartnerWebOAuth2ClientInfo().getApiPrefix() + WILD_CARD_URI);
        RequestMatcher thirdPartyApiUriMatcher = new AntPathRequestMatcher(applicationProperties.getThirdPartyOAuth2ClientInfo().getApiPrefix() + WILD_CARD_URI);
        RequestMatcher saleApiUriMatcher = new AntPathRequestMatcher(applicationProperties.getSaleAppOAuth2ClientInfo().getApiPrefix() + WILD_CARD_URI);
        oauth2ClientAclRuleTmp.put(applicationProperties.getVnskyWebOAuth2ClientInfo().getClientId(),
                new OrRequestMatcher(internalApiUriMatcher, partnerApiUriMatcher, thirdPartyApiUriMatcher)
        );
        oauth2ClientAclRuleTmp.put(applicationProperties.getPartnerWebOAuth2ClientInfo().getClientId(), partnerApiUriMatcher);
        oauth2ClientAclRuleTmp.put(applicationProperties.getThirdPartyOAuth2ClientInfo().getClientId(), thirdPartyApiUriMatcher);
        oauth2ClientAclRuleTmp.put(applicationProperties.getSaleAppOAuth2ClientInfo().getClientId(), saleApiUriMatcher);
        this.oauth2ClientAclRule = Collections.unmodifiableMap(oauth2ClientAclRuleTmp);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        if (request.getUserPrincipal() instanceof Authentication authentication &&
            authentication.getPrincipal() instanceof UserDTO userDTO) {
            String apiUriPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String appCode = userDTO.getAttribute("appCode");
            String requestUri = request.getRequestURI();
            
            // Log for debugging
            log.debug("ACL Check - URI: {}, Pattern: {}, AppCode: {}, Method: {}", 
                requestUri, apiUriPattern, appCode, request.getMethod());
            
            if (appCode == null || appCode.isEmpty()) {
                log.error("ACL Check failed - appCode is null or empty for user: {}, URI: {}", 
                    userDTO.getUsername(), requestUri);
                throw new AccessDeniedException("OAuth2 client has not been granted to access this resource: appCode is missing");
            }
            
            RequestMatcher requestMatcher = oauth2ClientAclRule.get(appCode);
            if (requestMatcher == null) {
                log.error("ACL Check failed - No RequestMatcher found for appCode: {}, URI: {}, Available appCodes: {}", 
                    appCode, requestUri, oauth2ClientAclRule.keySet());
                throw new AccessDeniedException("OAuth2 client has not been granted to access this resource: appCode '" + appCode + "' not found in ACL rules");
            }
            
            if (!requestMatcher.matches(request)) {
                log.error("ACL Check failed - Request URI '{}' does not match pattern for appCode: {}", 
                    requestUri, appCode);
                throw new AccessDeniedException("OAuth2 client has not been granted to access this resource: URI pattern mismatch");
            }
            PolicyCheckDTO checkAclRequest = PolicyCheckDTO.builder()
                    .appCode(appCode)
                    .userId(userDTO.getId())
                    .serviceCode(serviceCode)
                    .uriPattern(apiUriPattern)
                    .method(request.getMethod())
                    .build();
            PolicyCheckDTO checkAclResponse = this.objectService.checkPolicy(checkAclRequest);
            UserDTO currentUserDTO = checkAclResponse.getUser();
            if (Objects.isNull(currentUserDTO)) {
                throw new BadCredentialsException("User not found");
            }
            if (!Objects.equals(currentUserDTO.getStatus(), AuthConstants.ModelStatus.ACTIVE)) {
                throw new DisabledException("User is not active");
            }
//            if (!Boolean.TRUE.equals(checkAclResponse.getGranted())) {
//                throw new AccessDeniedException("User has not been granted to access this resource");
//            }
            if (Objects.nonNull(userDTO.getLastModifiedDate()) &&
                currentUserDTO.getLastModifiedDate().truncatedTo(ChronoUnit.SECONDS).isAfter(userDTO.getLastModifiedDate())) {
                userDTO.setClient(currentUserDTO.getClient());
                userDTO.setFullname(currentUserDTO.getFullname());
            }
        }
        return true;
    }

}
