package vn.vnsky.bcss.admin.mock;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import vn.vnsky.bcss.admin.dto.ClientDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UserDTO principal =
                UserDTO.builder()
                        .id(annotation.id())
                        .username(annotation.username())
                        .fullname(annotation.fullname())
                        .email(annotation.email())
                        .phoneNumber(annotation.phoneNumber())
                        .gender(annotation.gender())
                        .client(ClientDTO.builder()
                                .id(annotation.clientId())
                                .code(annotation.clientCode())
                                .name(annotation.clientName())
                                .build())
                        .build();
        principal.setAttribute("appId", annotation.appId());
        principal.setAttribute("appCode", annotation.appCode());
        principal.setAttribute("appName", annotation.appName());
        Authentication auth =
                new PreAuthenticatedAuthenticationToken(principal, null, principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }

}
