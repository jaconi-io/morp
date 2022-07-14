package io.jaconi.morp.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jaconi.morp.oauth.TenantAwareClientRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

@EnableCaching
@Configuration
@RequiredArgsConstructor
public class CacheConfiguration {

    @Autowired
    private final CacheProperties cacheProperties;

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder
                .withCacheConfiguration(TenantAwareClientRegistrationRepository.REGISTRATIONS,
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(cacheProperties.durationInMinutes())))
                .withCacheConfiguration(TenantAwareClientRegistrationRepository.FALLBACK,
                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ZERO));
    }

    @Bean
    public CacheManagerCustomizer<CaffeineCacheManager> caffeineCacheManagerCustomizer() {
        return cacheManager -> {
            cacheManager.registerCustomCache(TenantAwareClientRegistrationRepository.REGISTRATIONS,
                    createCaffeineCache(cacheProperties.durationInMinutes()));
            cacheManager.registerCustomCache(TenantAwareClientRegistrationRepository.FALLBACK,
                    createCaffeineCache(0));
        };
    }

    private @NonNull Cache<Object, Object> createCaffeineCache(int expirationInMinutes) {
        var builder = Caffeine.newBuilder();
        if (expirationInMinutes > 0) {
            builder.expireAfterWrite(Duration.ofMinutes(expirationInMinutes));
        }
        return builder.build();
    }
}
