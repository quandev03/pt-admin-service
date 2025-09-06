package vn.vnsky.bcss.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.common.serialization.*;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.kafka.support.converter.BatchMessageConverter;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;
import vn.vnsky.bcss.admin.config.kafka.KafkaAuthConsumerInterceptor;
import vn.vnsky.bcss.admin.config.kafka.KafkaAuthProducerInterceptor;
import vn.vnsky.bcss.admin.tracing.RequestAuthInfoProvider;

@Configuration
public class KafkaConfig {

    @Bean
    public DefaultKafkaConsumerFactoryCustomizer consumerCustomizer() {
        return consumerFactory -> {
            DefaultKafkaConsumerFactory<String, byte[]> customConsumerFactory = (DefaultKafkaConsumerFactory<String, byte[]>) consumerFactory;
            Deserializer<String> keyDeserializer = new StringDeserializer();
            Deserializer<byte[]> valueDeserializer = new ByteArrayDeserializer();
            customConsumerFactory.setKeyDeserializer(keyDeserializer);
            customConsumerFactory.setValueDeserializer(valueDeserializer);
        };
    }

    @Bean
    public DefaultKafkaProducerFactoryCustomizer producerCustomizer(ObjectMapper objectMapper) {
        return producerFactory -> {
            DefaultKafkaProducerFactory<String, Object> customProducerFactory = (DefaultKafkaProducerFactory<String, Object>) producerFactory;
            Serializer<String> keySerializer = new StringSerializer();
            Serializer<Object> valueSerializer = new JsonSerializer<>(objectMapper);
            customProducerFactory.setKeySerializer(keySerializer);
            customProducerFactory.setValueSerializer(valueSerializer);
        };
    }

    @Bean
    public ProducerInterceptor<String, Object> authProducerInterceptor(ObjectMapper objectMapper,
                                                                       KafkaOperations<String, Object> kafkaOperations,
                                                                       RequestAuthInfoProvider requestAuthInfoProvider) {
        ProducerInterceptor<String, Object> producerInterceptor = new KafkaAuthProducerInterceptor(objectMapper, requestAuthInfoProvider);
        if (kafkaOperations instanceof KafkaTemplate<String, Object> kafkaTemplate) {
            kafkaTemplate.setProducerInterceptor(producerInterceptor);
        }
        return producerInterceptor;
    }

    @Bean
    public RecordInterceptor<Object, Object> authConsumerInterceptor() {
        return new KafkaAuthConsumerInterceptor();
    }

    @Bean
    public RecordMessageConverter recordMessageConverter() {
        return new JsonMessageConverter();
    }

    @Bean
    public BatchMessageConverter batchMessageConverter() {
        return new BatchMessagingMessageConverter(recordMessageConverter());
    }

}
