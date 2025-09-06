package vn.vnsky.bcss.admin.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

import java.util.HashMap;
import java.util.Map;


@Configuration
@Profile("test")
public class EmbeddedKafkaConfig {

    @Bean
    public EmbeddedKafkaBroker embeddedKafkaBroker() {
        Map<String, String> properties = new HashMap<>();
        properties.put("listeners", "PLAINTEXT://localhost:9092");
        properties.put("advertised.listeners", "PLAINTEXT://localhost:9092");
        properties.put("listener.security.protocol.map", "PLAINTEXT:PLAINTEXT");
        EmbeddedKafkaBroker embeddedKafkaBroker = new EmbeddedKafkaZKBroker(1, false, 3, "system-access-log", "system-audit-log");
        embeddedKafkaBroker
                .kafkaPorts(9092)
                .brokerProperties(properties)
                .brokerListProperty("spring.kafka.bootstrap-servers");
        return embeddedKafkaBroker;
    }

}
