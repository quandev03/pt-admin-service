package vn.vnsky.bcss.admin.mock;

import com.github.fppt.jedismock.RedisServer;
import com.github.fppt.jedismock.server.ServiceOptions;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@Profile("test")
public class EmbeddedRedisConfig {

    private final RedisServer redisServer;

    public EmbeddedRedisConfig() {
        this.redisServer = RedisServer
                .newRedisServer(6379)
                .setOptions(ServiceOptions.defaultOptions()
                        .withClusterModeEnabled());
    }

    @PostConstruct
    public void postConstruct() throws IOException {
        redisServer.start();
    }

    @PreDestroy
    public void preDestroy() throws IOException {
        redisServer.stop();
    }

}
