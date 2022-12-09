package io.jaconi.morp.session;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

@Configuration
@EnableRedisWebSession
@ConditionalOnProperty(prefix = "morp.session", name = "store-type", havingValue = "redis")
public class RedisSessionConfiguration {

}
