package vn.vnsky.bcss.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.cors", ignoreUnknownFields = false)
public class CorsProperties {

    private List<String> exposeHeaders = List.of("*");

    private List<String> allowedOriginPatterns = List.of("*");

    private List<String> allowedMethods = List.of("*");

    private List<String> allowedHeaders = List.of("*");

    private boolean allowCredentials = false;

    private boolean allowPrivateNetwork = true;

}