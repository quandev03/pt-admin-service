package vn.vnsky.bcss.admin.config;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private String version = "1.0";

    private String defaultPass = "";

    private Duration passwordExpireTime = Duration.ofDays(90);

    private OAuth2WebClientInfo vnskyWebOAuth2ClientInfo;

    private OAuth2WebClientInfo partnerWebOAuth2ClientInfo;

    private OAuth2WebClientInfo thirdPartyOAuth2ClientInfo;

    private OAuth2WebClientInfo saleAppOAuth2ClientInfo;

    private CorsProperties cors;

    private String customTrustedCert = "";

    private String googleCloudApiKey = "";

    private String groupUserApiKey = "";

    private Map<String, OAuth2WebClientInfo> webClientInforMap = new HashMap<>();

    @PostConstruct
    private void initWebClientInfor(){
        webClientInforMap.put(vnskyWebOAuth2ClientInfo.getId(), vnskyWebOAuth2ClientInfo);
        webClientInforMap.put(partnerWebOAuth2ClientInfo.getId(), partnerWebOAuth2ClientInfo);
        webClientInforMap.put(thirdPartyOAuth2ClientInfo.getId(), thirdPartyOAuth2ClientInfo);
        webClientInforMap.put(saleAppOAuth2ClientInfo.getId(), saleAppOAuth2ClientInfo);
    }

    public OAuth2WebClientInfo getOauth2WebClientInfoById(String id) {
        return webClientInforMap.get(id);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OAuth2WebClientInfo {

        private String id;

        private String clientId;

        private String clientName;

        private String clientSecret;

        private String url;

        private String apiPrefix;

        private Duration defaultAccessTokenValidity = Duration.ofMinutes(30);

        private Duration defaultRefreshTokenValidity = Duration.ofDays(1);

        private boolean reuseRefreshToken = true;

    }

}
