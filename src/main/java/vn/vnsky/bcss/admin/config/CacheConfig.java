package vn.vnsky.bcss.admin.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import vn.vnsky.bcss.admin.constant.CacheKey;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@EnableCaching
@Configuration
public class CacheConfig {

    private final Map<String, Duration> cacheExpiryMap;

    @Autowired
    public CacheConfig() {
        Map<String, Duration> tmpMap = new HashMap<>();
        tmpMap.put(
                CacheKey.USER_FORGOT_PASSWORD_TOKEN_PREFIX, Duration.ofMinutes(5));
        tmpMap.put(
                CacheKey.USER_INFO_PREFIX, Duration.ofHours(1));
        tmpMap.put(
                CacheKey.USER_POLICY_PREFIX, Duration.ofHours(1));
        tmpMap.put(
                CacheKey.API_ACL_PREFIX, Duration.ofHours(1));
        this.cacheExpiryMap = Collections.unmodifiableMap(tmpMap);
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new Hibernate6Module());
        objectMapper.registerModule(new AfterburnerModule());
        objectMapper.enable(JsonGenerator.Feature.IGNORE_UNKNOWN);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        return RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(CacheKey.CACHE_KEY_PREFIX)
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer))
                ;
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> cacheExpiryMap.forEach((cacheName, ttl) -> builder
                .withCacheConfiguration(cacheName,
                        cacheConfiguration()
                                .entryTtl(ttl)));
    }

}
