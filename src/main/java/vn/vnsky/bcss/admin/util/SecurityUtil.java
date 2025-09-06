package vn.vnsky.bcss.admin.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.constant.AuthoritiesConstants;
import vn.vnsky.bcss.admin.dto.ClientDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityUtil {

    public static Optional<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDTO userDTO) {
            return Optional.of(userDTO);
        }
        return Optional.empty();
    }

    public static String getCurrentUserId() {
        return getCurrentUser().map(UserDTO::getId).orElse(null);
    }

    public static String getCurrentUserName() {
        return getCurrentUser().map(UserDTO::getUsername).orElse(null);
    }

    public static String getCurrentFullName() {
        return getCurrentUser().map(UserDTO::getFullname).orElse(null);
    }

    public static String getCurrentAppId() {
        return getCurrentUser()
                .map(UserDTO::getAttributes)
                .map(e -> (String) e.get("appId"))
                .orElse(null);
    }

    public static String getCurrentAppCode() {
        return getCurrentUser()
                .map(UserDTO::getAttributes)
                .map(e -> (String) e.get("appCode"))
                .orElse(null);
    }

    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .map(authentication -> {
                    if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                        return userDetails.getUsername();
                    } else if (authentication.getPrincipal() instanceof String userLogin) {
                        return userLogin;
                    }
                    return null;
                });
    }

    /**
     * Get the JWT of the current user.
     *
     * @return the JWT of the current user.
     */
    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(authentication -> authentication.getCredentials() instanceof String)
                .map(authentication -> (String) authentication.getCredentials());
    }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise.
     */
    public static boolean isAuthenticated() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .map(authentication -> authentication.getAuthorities().stream()
                        .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(AuthoritiesConstants.ANONYMOUS)))
                .orElse(false);
    }

    /**
     * If the current user has a specific authority (security role).
     * <p>
     * The name of this method comes from the {@code isUserInRole()} method in the Servlet API.
     *
     * @param authority the authority to check.
     * @return true if the current user has the authority, false otherwise.
     */
    public static boolean isCurrentUserInRole(String authority) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .map(authentication -> authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority)))
                .orElse(false);
    }

    public static String getCurrentClientId() {
        return getCurrentUser()
                .map(UserDTO::getClient)
                .map(ClientDTO::getId)
                .orElse(null);
    }

    public static String getCurrentClientCode() {
        return getCurrentUser()
                .map(UserDTO::getClient)
                .map(ClientDTO::getCode)
                .orElse(null);
    }

    public static String getPreferredUsername(UserDTO userDTO) {
        String clientIdentity = StringUtils.hasText(userDTO.getClient().getCode()) ? userDTO.getClient().getCode() : userDTO.getClient().getId();
        if (AuthConstants.VNSKY_CLIENT_ALIAS.equals(clientIdentity) || AuthConstants.VNSKY_CLIENT_ID.equals(clientIdentity)) {
            return userDTO.getUsername();
        }
        return clientIdentity + AuthConstants.CommonSymbol.BACKSLASH + userDTO.getUsername();
    }

    public static String getUniqueUsername(UserDTO userDTO) {
        return userDTO.getClient().getId() + AuthConstants.CommonSymbol.BACKSLASH + userDTO.getUsername();
    }
}

