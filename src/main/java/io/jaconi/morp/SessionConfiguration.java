package io.jaconi.morp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SessionConfiguration {

    @EnableRedisWebSession
    @ConditionalOnProperty(prefix = "morp.session", name = "store-type", havingValue = "redis")
    public static class RedisSessionConfiguration {
    }

    @ConditionalOnProperty(prefix = "morp.session", name = "store-type", havingValue = "none")
    public static class MapSessionConfiguration {
        @Bean
        public ReactiveMapSessionRepository sessionRepository() {
            return new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
        }
    }

}
