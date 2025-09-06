package vn.vnsky.bcss.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.transaction.PlatformTransactionManager;
import vn.vnsky.bcss.admin.aop.AuditAspect;
import vn.vnsky.bcss.admin.aop.LoggingAspect;
import vn.vnsky.bcss.admin.config.auth.OAuth2JdbcAspect;
import vn.vnsky.bcss.admin.constant.AuthConstants;


@Slf4j
@Configuration
@EnableAspectJAutoProxy
public class AspectConfig {

    @Bean
    @Profile(AuthConstants.SPRING_PROFILE_DEVELOPMENT)
    public LoggingAspect loggingAspect(Environment env) {
        return new LoggingAspect(env);
    }

    @Bean
    public AuditAspect auditAspect(KafkaOperations<String, Object> kafkaOperations) {
        return new AuditAspect(kafkaOperations);
    }

    @Bean
    public OAuth2JdbcAspect oAuth2JdbcAspect(PlatformTransactionManager platformTransactionManager) {
        return new OAuth2JdbcAspect(platformTransactionManager);
    }
}
