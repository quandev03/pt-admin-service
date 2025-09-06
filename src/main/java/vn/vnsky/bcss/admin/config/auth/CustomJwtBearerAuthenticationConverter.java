package vn.vnsky.bcss.admin.config.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.StringUtils;
import vn.vnsky.bcss.admin.config.JwtProperties;
import vn.vnsky.bcss.admin.dto.AppDTO;
import vn.vnsky.bcss.admin.dto.ClientDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;
import vn.vnsky.bcss.admin.util.SecurityUtil;

import java.time.*;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class CustomJwtBearerAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken>,
        OAuth2TokenCustomizer<JwtEncodingContext> {

    private final ZoneOffset zoneOffset;

    private final JwtProperties jwtProperties;

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    public CustomJwtBearerAuthenticationConverter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("user_authorities");
        this.jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        OffsetDateTime odt = OffsetDateTime.now();
        this.zoneOffset = odt.getOffset();
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt());
        String userId = jwt.getClaimAsString("user_id");
        UserDTO principal = UserDTO.builder()
                .id(userId)
                .username(jwt.getClaimAsString("username"))
                .type(jwt.getClaimAsString("user_type"))
                .fullname(jwt.getClaimAsString("fullname"))
                .client(ClientDTO.builder()
                        .id(jwt.getClaimAsString("client_id"))
                        .code(jwt.getClaimAsString("client_code"))
                        .name(jwt.getClaimAsString("client_name"))
                        .build())
                .build();
        principal.setAttribute("appId", jwt.getClaimAsString("app_id"));
        principal.setAttribute("appCode", jwt.getClaimAsString("app_code"));
        principal.setAttribute("appName", jwt.getClaimAsString("app_name"));
        String ssoProvider = jwt.getClaimAsString("sso_provider");
        if (StringUtils.hasText(ssoProvider)) {
            principal.setAttribute(AppDTO.Fields.ssoProvider, ssoProvider);
        }
        Instant lastModifiedDate = jwt.getClaimAsInstant("last_modified_date");
        if (Objects.nonNull(lastModifiedDate)) {
            principal.setLastModifiedDate(LocalDateTime.ofInstant(lastModifiedDate, this.zoneOffset));
        }
        Collection<GrantedAuthority> authorities = this.jwtGrantedAuthoritiesConverter.convert(jwt);
        return new BearerTokenAuthentication(principal, accessToken, authorities);
    }

    @Override
    public void customize(JwtEncodingContext context) {
        UserDTO userDTO = (UserDTO) context.getPrincipal().getPrincipal();
        context.getJwsHeader()
                .header("alg", SignatureAlgorithm.valueOf(jwtProperties.getAlgorithm()))
                .header("typ", "JWT");
        JwtClaimsSet.Builder claims = context.getClaims();
        claims.subject(SecurityUtil.getUniqueUsername(userDTO));
        claims.claim("client_id", userDTO.getClient().getId());
        if (StringUtils.hasText(userDTO.getClient().getCode())) {
            claims.claim("client_code", userDTO.getClient().getCode());
        }
        claims.claim("client_name", userDTO.getClient().getName());
        claims.claim("user_id", userDTO.getId());
        claims.claim("username", userDTO.getUsername());
        if (StringUtils.hasText(userDTO.getType())) {
            claims.claim("user_type", userDTO.getType());
        }
        claims.claim("fullname", userDTO.getFullname());
        Collection<String> authorities = userDTO.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        claims.claim("user_authorities", authorities);
        claims.claim("preferred_username", SecurityUtil.getPreferredUsername(userDTO));
        AppDTO appDTO = (AppDTO) context.getPrincipal().getDetails();
        claims.claim("app_id", appDTO.getId());
        claims.claim("app_code", appDTO.getCode());
        claims.claim("app_name", appDTO.getName());
        claims.claim("last_modified_date", userDTO.getLastModifiedDate().toEpochSecond(this.zoneOffset));
        if (StringUtils.hasText(appDTO.getSsoProvider())) {
            claims.claim("sso_provider", appDTO.getSsoProvider());
        }
        claims.notBefore(LocalDateTime.now().minusSeconds(this.jwtProperties.getClockSkewTolerance()).toInstant(this.zoneOffset));
    }
}
