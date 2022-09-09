package io.jaconi.morp.oauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Set;

import static io.jaconi.morp.oauth.TenantAwareClientRegistrationRepository.REGISTRATIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ClientRegistrationActuatorTest {

    @InjectMocks
    private ClientRegistrationActuator actuator;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testRegistrationsWithUnsupportedCacheBackend() {
        assertThatThrownBy(() -> actuator.getRegistrations())
                .isInstanceOf(HttpServerErrorException.class)
                .hasMessage("500 Only redis, caffeine, and simple are supported as cache type.");
    }

    @Test
    void testRegistrationsWithCaffeineCache() {

        // create a standalone Caffeine cache
        var cm = new CaffeineCacheManager(REGISTRATIONS);
        cm.setCacheLoader(key -> key);

        // access the cache so we have a value in there
        var cache = cm.getCache(REGISTRATIONS);
        assertThat(cache.get("tenant2", String.class)).isNotNull();
        assertThat(cache.get("tenant1", String.class)).isNotNull();

        // mock the cache manager to return the Caffeine Cache
        doReturn(cache).when(cacheManager).getCache(REGISTRATIONS);

        // expect the tenants to be returned in sorted order
        assertThat(actuator.getRegistrations()).containsOnly("tenant1", "tenant2");
    }

    @Test
    void testRegistrationsWithLocalCache() {

        // create a local cache and mock it into the cache manager
        var cache = new ConcurrentMapCache(REGISTRATIONS);
        cache.put("tenant2", "foo");
        cache.put("tenant1", "boo");

        // mock the cache manager to return the cache instance
        doReturn(cache).when(cacheManager).getCache(REGISTRATIONS);

        // expect the tenants to be returned in sorted order
        assertThat(actuator.getRegistrations()).containsOnly("tenant1", "tenant2");
    }

    @Test
    void testRegistrationsWithRedisCache() {

        // simulate that the cache manager uses a RedisCache
        doReturn(mock(RedisCache.class)).when(cacheManager).getCache(REGISTRATIONS);

        // make the Redis client return something
        var keys = Set.of(REGISTRATIONS + "::tenant2", REGISTRATIONS + "::tenant1");
        doReturn(keys).when(redisTemplate).keys(REGISTRATIONS + "*");

        // expect the tenants to be returned in sorted order
        assertThat(actuator.getRegistrations()).containsOnly("tenant1", "tenant2");
    }

    @Test
    void testRegistration() {

        // create a simple client registration for testing
        var registration = ClientRegistration.withRegistrationId("testId")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("client-id")
                .clientSecret("client-secret")
                .redirectUri("*")
                .authorizationUri("*")
                .tokenUri("*")
                .build();

        // mock the cache manager to return the cache and its registration
        var cache = mock(Cache.class);
        doReturn(cache).when(cacheManager).getCache(REGISTRATIONS);
        doReturn(registration).when(cache).get("tenant1", ClientRegistration.class);

        // validate the returned client id with secret redaction
        assertThat(actuator.getRegistration("tenant1")).satisfies(r -> {
            assertThat(r).isNotSameAs(registration);
            assertThat(r.getRegistrationId()).isEqualTo("testId");
            assertThat(r.getClientId()).isEqualTo("client-id");
            assertThat(r.getClientSecret()).isEqualTo("******");
        });
    }

    @Test
    void testRegistrationWithMissingCache() {
        doReturn(null).when(cacheManager).getCache(REGISTRATIONS);
        assertThat(actuator.getRegistration("tenant1")).isNull();
    }

    @Test
    void testRegistrationWithEmptyCache() {
        // mock an empty cache and expect null as lookup result
        var cache = mock(Cache.class);
        doReturn(cache).when(cacheManager).getCache(REGISTRATIONS);
        assertThat(actuator.getRegistration("tenant1")).isNull();
    }
}
