package vn.vnsky.bcss.admin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.task.SimpleAsyncTaskExecutorCustomizer;
import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.vnsky.bcss.admin.tracing.AsyncTaskTracingCustomizer;
import vn.vnsky.bcss.admin.tracing.ThreadPoolTaskTracingCustomizer;

@Configuration
public class TracingConfig {

    @Bean
    @ConditionalOnProperty(value = "application.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public SimpleAsyncTaskExecutorCustomizer asyncTaskTracingCustomizer() {
        return new AsyncTaskTracingCustomizer();
    }

    @Bean
    @ConditionalOnProperty(value = "application.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public ThreadPoolTaskExecutorCustomizer threadPoolTaskTracingCustomizer() {
        return new ThreadPoolTaskTracingCustomizer();
    }

}
