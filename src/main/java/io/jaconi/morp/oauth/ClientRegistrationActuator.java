package io.jaconi.morp.oauth;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.jaconi.morp.oauth.TenantAwareClientRegistrationRepository.REGISTRATIONS;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.springframework.boot.actuate.endpoint.SanitizableData.SANITIZED_VALUE;

@Component
@Endpoint(id = "clientregistrations")
public class ClientRegistrationActuator {

    private final CacheManager cacheManager;

    private final StringRedisTemplate redisTemplate;

    public ClientRegistrationActuator(CacheManager cacheManager, StringRedisTemplate redisTemplate) {
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }

    @ReadOperation
    public List<String> getRegistrations() {
        Cache cache = cacheManager.getCache(REGISTRATIONS);

        Set<?> keys;
        if (cache instanceof CaffeineCache c) {
            keys = c.getNativeCache().asMap().keySet();
        } else if (cache instanceof ConcurrentMapCache c) {
            keys = c.getNativeCache().keySet();
        } else if (cache instanceof RedisCache) {
            keys = redisTemplate.keys(REGISTRATIONS + "*")
                    .stream()
                    .map(k -> substringAfter(k, "::"))
                    .collect(toSet());
        } else {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Only redis, caffeine, and simple are supported as cache type.");
        }

        // ensure stable, sorted order for actuator
        return keys.stream()
                .map(Object::toString)
                .sorted()
                .toList();
    }

    @ReadOperation
    public ClientRegistration getRegistration(@Selector String tenant) {
        return Optional.ofNullable(cacheManager.getCache(REGISTRATIONS))
                .flatMap(cache -> Optional.ofNullable(cache.get(tenant, ClientRegistration.class)))
                .map(r -> ClientRegistration.withClientRegistration(r).clientSecret(SANITIZED_VALUE).build())
                .orElse(null);
    }
}
