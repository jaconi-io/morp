package io.jaconi.morp.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantAwareClientRegistrationRepository implements ReactiveClientRegistrationRepository {

    public static final String FALLBACK = "fallback";
    public static final String REGISTRATIONS = "registrations";
    private final CacheManager cacheManager;

    private final ClientRegistrationFetcher clientRegistrationFetcher;

    @Override
    public Mono<ClientRegistration> findByRegistrationId(String tenant) {
        return Mono.fromSupplier(() -> this.getRegistration(tenant));
    }

    private ClientRegistration getRegistration(String tenant) {
        try {
            var registration = getCached(tenant);
            if (registration != null) {
                log.debug("Got Client Registration for tenant '{}' from cache.", tenant);
                return registration;
            }
            registration = clientRegistrationFetcher.getRegistration(tenant);
            if (registration != null) {
                log.debug("Putting Client Registration for tenant '{}' in cache.", tenant);
                putInCache(tenant, registration);
                return registration;
            }
        } catch (RuntimeException e) {
            log.error(String.format("Error creating client registration for tenant '%s'.", tenant), e);
        }
        log.debug("Getting Client Registration for tenant '{}' from fallback cache.", tenant);
        return getFallBack(tenant);
    }

    private void putInCache(String tenant, ClientRegistration registration) {
        cacheManager.getCache(REGISTRATIONS).put(tenant, registration);
        cacheManager.getCache(FALLBACK).put(tenant, registration);
    }

    private ClientRegistration getCached(String tenant) {
        return cacheManager.getCache(REGISTRATIONS).get(tenant, ClientRegistration.class);
    }

    private ClientRegistration getFallBack(String tenant) {
        return cacheManager.getCache(FALLBACK).get(tenant, ClientRegistration.class);
    }

}
