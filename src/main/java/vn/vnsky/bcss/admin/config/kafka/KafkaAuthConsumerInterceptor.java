package vn.vnsky.bcss.admin.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
public class KafkaAuthConsumerInterceptor implements RecordInterceptor<Object, Object> {

    @Override
    public void clearThreadState(@NonNull Consumer<?, ?> consumer) {
        KafkaAuthContextHolder.clear();
    }

    @Override
    public ConsumerRecord<Object, Object> intercept(@NonNull ConsumerRecord<Object, Object> consumerRecord, @NonNull Consumer<Object, Object> consumer) {
        log.info("[LOGGING_MESSAGE] - topic: {} => message consumed: key {}", consumerRecord.topic(), consumerRecord.key());
        if (log.isDebugEnabled()) {
            log.debug("[LOGGING_MESSAGE] - payload: {}", consumerRecord.value());
        }
        Headers headers = consumerRecord.headers();
        String userId = getHeaderValue(headers, "USER_ID");
        if (StringUtils.hasText(userId)) {
            String clientId = getHeaderValue(headers, "CLIENT_ID");
            String clientCode = getHeaderValue(headers, "CLIENT_CODE");
            String clientName = getHeaderValue(headers, "CLIENT_NAME");
            String siteId = getHeaderValue(headers, "SITE_ID");
            String siteCode = getHeaderValue(headers, "SITE_CODE");
            String siteName = getHeaderValue(headers, "SITE_NAME");
            String userFullname = getHeaderValue(headers, "USER_FULLNAME");
            String userUsername = getHeaderValue(headers, "USER_USERNAME");
            String userPreferredUsername = getHeaderValue(headers, "USER_PREFERRED_USERNAME");
            KafkaUserDTO kafkaUserDTO = KafkaUserDTO.builder()
                    .clientId(clientId)
                    .clientCode(clientCode)
                    .clientName(clientName)
                    .siteId(siteId)
                    .siteCode(siteCode)
                    .siteName(siteName)
                    .userId(userId)
                    .userUsername(userUsername)
                    .userPreferredUsername(userPreferredUsername)
                    .userFullname(userFullname)
                    .build();
            KafkaAuthContextHolder.setCurrent(kafkaUserDTO);
        } else {
            KafkaAuthContextHolder.clear();
        }
        return consumerRecord;
    }

    protected String getHeaderValue(Headers headers, String key) {
        Header header = headers.lastHeader(key);
        if (header != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return null;
    }

}
