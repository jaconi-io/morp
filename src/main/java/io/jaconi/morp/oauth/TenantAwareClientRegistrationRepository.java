package io.jaconi.morp.oauth;

import java.util.Objects;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantAwareClientRegistrationRepository implements ClientRegistrationRepository {

    public static final String REGISTRATIONS = "registrations";

    public static final String SOURCE_HASHES = "sources-hashes";

    private final CacheManager cacheManager;

    private final RegistrationResolver registrationResolver;

    private final ProviderResolver providerResolver;

    private final ClientRegistrationFetcher clientRegistrationFetcher;

    @Override
    public ClientRegistration findByRegistrationId(String tenant) {
        log.debug("Creating Client Registration for tenant '{}'.", tenant);

        var registration = registrationResolver.getRegistration(tenant);
        var provider = providerResolver.getProvider(tenant, registration.getProvider());
        var clientRegistrationSource = new ClientRegistrationSource(provider, registration);

        // Look up source in cache
        var cachedSourceHash = getCachedSourceHash(tenant);
        if (cachedSourceHash != null && cachedSourceHash.equals(clientRegistrationSource.sha256())) {
            // Nothing changed, return cached registration
            return getCachedRegistration(tenant);
        }

        try {
            var clientRegistration = clientRegistrationFetcher.getRegistration(tenant, clientRegistrationSource);
            if (clientRegistration != null) {
                log.debug("Putting Client Registration for tenant '{}' in cache.", tenant);
                putInCache(tenant, clientRegistrationSource.sha256(), clientRegistration);
                return clientRegistration;
            }
        } catch (RuntimeException e) {
            log.error(String.format("Error creating client registration for tenant '%s'.", tenant), e);
        }
        log.debug("Try getting Client Registration for tenant '{}' from cache after error.", tenant);
        return getCachedRegistration(tenant);
    }

    private void putInCache(String tenant, String registrationSourceHash, ClientRegistration registration) {
        getCache(SOURCE_HASHES).put(tenant, registrationSourceHash);
        getCache(REGISTRATIONS).put(tenant, registration);
    }

    private ClientRegistration getCachedRegistration(String tenant) {
        return getCache(REGISTRATIONS).get(tenant, ClientRegistration.class);
    }

    private String getCachedSourceHash(String tenant) {
        return getCache(SOURCE_HASHES).get(tenant, String.class);
    }

    private Cache getCache(String name) {
        return Objects.requireNonNull(cacheManager.getCache(name));
    }

}
