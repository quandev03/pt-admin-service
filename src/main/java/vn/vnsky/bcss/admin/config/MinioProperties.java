package vn.vnsky.bcss.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String url;

    private String accessKey;

    private String secretKey;

    private long connectTimeout = 30000;

    private long writeTimeout = 60000;

    private long readTimeout = 60000;

    private String bucketPublic;

    private String bucketPrivate;

    private String bucketPublicPolicy;
}
