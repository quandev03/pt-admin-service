package vn.vnsky.bcss.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String keyId = "vnsky";

    private String keyUse = "sig";

    private String algorithm = "RS256";

    private String rsaPublicKey = "";

    private String rsaPrivateKey = "";

    private Long clockSkewTolerance = 60L;

}
