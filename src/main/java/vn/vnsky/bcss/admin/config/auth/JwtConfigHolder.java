package vn.vnsky.bcss.admin.config.auth;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import vn.vnsky.bcss.admin.config.JwtProperties;
import vn.vnsky.bcss.admin.service.UserService;
import vn.vnsky.bcss.admin.util.CryptoUtil;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class JwtConfigHolder {

    @Getter
    private final OAuth2TokenGenerator<Jwt> jwtGenerator;

    @Getter
    private final JwtDecoder jwtDecoder;

    @Getter
    private final JwtEncoder jwtEncoder;

    @Getter
    private final JWKSource<SecurityContext> jwkSource;

    private final CustomJwtBearerAuthenticationConverter customJwtBearerAuthenticationConverter;

    @SneakyThrows
    public JwtConfigHolder(JwtProperties jwtProperties, UserService userService) {
        RSAPublicKey publicKey = CryptoUtil.readPublicKey(jwtProperties.getRsaPublicKey());
        RSAPrivateKey privateKey = CryptoUtil.readPrivateKey(jwtProperties.getRsaPrivateKey());
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(jwtProperties.getKeyId())
                .keyUse(KeyUse.parse(jwtProperties.getKeyUse()))
                .algorithm(Algorithm.parse(jwtProperties.getAlgorithm()))
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        this.jwkSource = new ImmutableJWKSet<>(jwkSet);
        this.jwtDecoder = OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
        this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
        this.customJwtBearerAuthenticationConverter = new CustomJwtBearerAuthenticationConverter(jwtProperties);
        JwtGenerator delegateJwtGenerator = new JwtGenerator(this.jwtEncoder);
        delegateJwtGenerator.setJwtCustomizer(this.customJwtBearerAuthenticationConverter);
        this.jwtGenerator = new CustomJwtGenerator(delegateJwtGenerator, userService);
    }

    public Converter<Jwt, AbstractAuthenticationToken> getJwtAuthenticationConverter() {
        return this.customJwtBearerAuthenticationConverter;
    }

    public OAuth2TokenCustomizer<JwtEncodingContext> getJwtCustomizer() {
        return this.customJwtBearerAuthenticationConverter;
    }

}
