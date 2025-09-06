package vn.vnsky.bcss.admin.config.auth;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import vn.vnsky.bcss.admin.constant.AuthConstants;

import java.util.Collections;

@EqualsAndHashCode(callSuper = true)
public class GoogleAuthenticationToken extends AbstractClientUserAuthenticationToken {

    private final String code;

    @Getter
    private final String origin;

    @Getter
    @Setter
    private String clientIdentity = AuthConstants.VNSKY_CLIENT_ALIAS;

    private UserDetails userDetails;

    public GoogleAuthenticationToken(String code, String origin) {
        super(Collections.emptyList());
        this.code = code;
        this.origin = origin;
    }

    @Override
    public Object getPrincipal() {
        return this.userDetails;
    }

    @Override
    public String getCredentials() {
        return code;
    }

    @Override
    public void makeAuthenticated(UserDetails userDetails) {
        super.makeAuthenticated(userDetails);
        this.userDetails = userDetails;
    }

}
