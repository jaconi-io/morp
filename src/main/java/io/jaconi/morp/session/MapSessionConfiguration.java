package io.jaconi.morp.session;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.ReactiveMapSessionRepository;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@ConditionalOnProperty(prefix = "morp.session", name = "store-type", havingValue = "none")
public class MapSessionConfiguration {

    @Bean
    public ReactiveMapSessionRepository sessionRepository() {
        return new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
    }

}
