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
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientRegistrationFetcher {

    public ClientRegistration getRegistration(String tenant, ClientRegistrationSource clientRegistrationSource) {
        log.debug("Creating Client Registration for tenant '{}' from scratch.", tenant);
        var properties = new SimpleOAuth2Properties(tenant, clientRegistrationSource);
        var clientRegistrations = OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(properties);
        return clientRegistrations.get(tenant);
    }
}
