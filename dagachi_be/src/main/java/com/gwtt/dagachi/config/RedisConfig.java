package com.gwtt.dagachi.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {
  @Value("${spring.data.redis.host}")
  private String host;

  @Value("${spring.data.redis.port}")
  private int port;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(host, port);
  }

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    ObjectMapper cacheObjectMapper = new ObjectMapper();

    PolymorphicTypeValidator validator =
        BasicPolymorphicTypeValidator.builder().allowIfSubType("com.gwtt.dagachi.dto").build();

    cacheObjectMapper.registerModule(new JavaTimeModule());
    cacheObjectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
    cacheObjectMapper.activateDefaultTyping(
        validator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    cacheObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    RedisSerializer<Object> serializer = new GenericJackson2JsonRedisSerializer(cacheObjectMapper);

    RedisCacheConfiguration config =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues();

    return RedisCacheManager.builder(redisConnectionFactory)
        .cacheDefaults(config)
        .withCacheConfiguration("posting", config.entryTtl(Duration.ofMinutes(5)))
        .withCacheConfiguration("postings", config.entryTtl(Duration.ofMinutes(1)))
        .withCacheConfiguration("participations", config.entryTtl(Duration.ofMinutes(3)))
        .build();
  }
}
