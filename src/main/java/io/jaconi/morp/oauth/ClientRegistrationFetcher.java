package io.jaconi.morp.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientRegistrationFetcher {

    private final RegistrationResolver registrationResolver;
    private final ProviderResolver providerResolver;

    public ClientRegistration getRegistration(String tenant) {
        log.debug("Creating Client Registration for tenant '{}'.", tenant);
        var provider = providerResolver.getProviders(tenant);
        var registration = registrationResolver.getRegistration(tenant);
        var properties = new SimpleOAuth2Properties(provider, Collections.singletonMap(tenant, registration));
        var clientRegistrations = OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(properties);
        return clientRegistrations.get(tenant);
    }
}
