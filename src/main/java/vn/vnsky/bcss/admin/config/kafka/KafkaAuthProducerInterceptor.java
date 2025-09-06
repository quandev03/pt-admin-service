package vn.vnsky.bcss.admin.config.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import vn.vnsky.bcss.admin.tracing.RequestAuthInfoProvider;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class KafkaAuthProducerInterceptor implements ProducerInterceptor<String, Object> {

    private final ObjectMapper objectMapper;

    private final RequestAuthInfoProvider requestAuthInfoProvider;

    public KafkaAuthProducerInterceptor(ObjectMapper objectMapper, RequestAuthInfoProvider requestAuthInfoProvider) {
        this.objectMapper = objectMapper;
        this.requestAuthInfoProvider = requestAuthInfoProvider;
    }

    @Override
    public ProducerRecord<String, Object> onSend(ProducerRecord<String, Object> producerRecord) {
        Map<String, Object> authHeaders = new HashMap<>();
        this.requestAuthInfoProvider.accept(authHeaders);
        authHeaders.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            if (value instanceof String valueStr) {
                producerRecord.headers().add(key, valueStr.getBytes(StandardCharsets.UTF_8));
            } else {
                try {
                    producerRecord.headers().add(key, this.objectMapper.writeValueAsBytes(value));
                } catch (JsonProcessingException ex) {
                    log.error("[KAFKA_AUTH_PRODUCER_INTERCEPTOR] Error while serializing value to bytes: {}", value, ex);
                }
            }
        });
        return producerRecord;
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        if (log.isDebugEnabled()) {
            log.debug("[KAFKA_AUTH_PRODUCER_INTERCEPTOR] Record acknowledged, metadata: {}", metadata);
        }
        if (exception != null) {
            log.error("[KAFKA_AUTH_PRODUCER_INTERCEPTOR] Record not acknowledged, metadata: {}", metadata, exception);
        }
    }

    @Override
    public void close() {
        if (log.isDebugEnabled()) {
            log.debug("[KAFKA_AUTH_PRODUCER_INTERCEPTOR] Record closed");
        }
    }

    @Override
    public void configure(Map<String, ?> configs) {
        if (log.isDebugEnabled()) {
            log.debug("[KAFKA_AUTH_PRODUCER_INTERCEPTOR] Configure not implemented");
        }
    }

}
