package vn.vnsky.bcss.admin.config.auth;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.UserDetails;
import vn.vnsky.bcss.admin.dto.UserDTO;

import java.io.Serial;


@Getter
@EqualsAndHashCode(callSuper = false)
public class OAuth2UsernamePasswordAuthenticationToken extends AbstractClientUserAuthenticationToken {

    @Serial
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    @Getter
    @Setter
    protected String clientIdentity;

    private UserDetails userDetails;

    private String credentials;

    public OAuth2UsernamePasswordAuthenticationToken(UserDTO userDetails, String credentials,
                                                     String clientIdentity) {
        super(null);
        this.userDetails = userDetails;
        this.credentials = credentials;
        this.clientIdentity = clientIdentity;
    }

    @Override
    public Object getPrincipal() {
        return userDetails;
    }

    @Override
    public void makeAuthenticated(UserDetails userDetails) {
        super.makeAuthenticated(userDetails);
        this.userDetails = userDetails;
        this.credentials = null;
    }

}
