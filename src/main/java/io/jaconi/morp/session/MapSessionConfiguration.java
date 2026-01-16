package io.jaconi.morp.session;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSessionRepository;

@Configuration
@ConditionalOnProperty(prefix = "morp.session", name = "store-type", havingValue = "none")
public class MapSessionConfiguration {

	@Bean
	public MapSessionRepository sessionRepository() {
		return new MapSessionRepository(new ConcurrentHashMap<>());
	}

}
