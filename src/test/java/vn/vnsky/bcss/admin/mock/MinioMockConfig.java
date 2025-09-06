package vn.vnsky.bcss.admin.mock;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("test")
public class MinioMockConfig {

    @Bean
    public MinioClient minioClient() {
        return mock(MinioClient.class);
    }

}
