package vn.vnsky.bcss.admin.config.auth;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.dto.UserDTO;
import vn.vnsky.bcss.admin.service.UserService;

import java.util.Objects;

@Slf4j
public class CustomJwtGenerator implements OAuth2TokenGenerator<Jwt>  {

    private final JwtGenerator delegateJwtGenerator;

    private final UserService userService;

    public CustomJwtGenerator(JwtGenerator jwtGenerator, UserService userService) {
        this.delegateJwtGenerator = jwtGenerator;
        this.userService = userService;
    }

    @Override
    public Jwt generate(OAuth2TokenContext context) {
        if (context.getPrincipal().getPrincipal() instanceof UserDTO userDTO) {
            try {
                UserDTO currentUserDTO = this.userService.detail(userDTO.getClient().getId(), userDTO.getId());
                if (!Objects.equals(currentUserDTO.getStatus(), AuthConstants.ModelStatus.ACTIVE)) {
                    throw new DisabledException("User is not active");
                }
                userDTO.setClient(currentUserDTO.getClient());
                userDTO.setFullname(currentUserDTO.getFullname());
            } catch (EntityNotFoundException ex) {
                log.error("User not found: ", ex);
                throw new BadCredentialsException("User not found");
            }
        }
        return this.delegateJwtGenerator.generate(context);
    }

}
