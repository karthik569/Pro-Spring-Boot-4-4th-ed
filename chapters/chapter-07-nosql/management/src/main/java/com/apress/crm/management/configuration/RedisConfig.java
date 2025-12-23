package com.apress.crm.management.configuration;

import com.apress.crm.management.model.CustomerSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisOperations<String, CustomerSession> customerSessionOps(ReactiveRedisConnectionFactory factory) {
        RedisSerializer<CustomerSession> serializer = new Jackson2JsonRedisSerializer<>(CustomerSession.class);

        RedisSerializationContext<String, CustomerSession> context = RedisSerializationContext
                .<String, CustomerSession>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}