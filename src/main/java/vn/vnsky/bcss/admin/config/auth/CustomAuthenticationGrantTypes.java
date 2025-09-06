package vn.vnsky.bcss.admin.config.auth;

import lombok.experimental.UtilityClass;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@UtilityClass
public class CustomAuthenticationGrantTypes {

    public static final AuthorizationGrantType PWD = new AuthorizationGrantType("password");

    public static final AuthorizationGrantType SSO = new AuthorizationGrantType("sso");
}
