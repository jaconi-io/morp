package io.jaconi.morp.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class TenantAwareClientRegistrationRepository implements ReactiveClientRegistrationRepository {
    private final Map<String, Mono<ClientRegistration>> clientCache = new ConcurrentHashMap<>();
    private final RegistrationResolver registrationResolver;
    private final ProviderResolver providerResolver;

    @Override
    public Mono<ClientRegistration> findByRegistrationId(String tenant) {
        return clientCache.computeIfAbsent(tenant, this::getClientRegistration);
    }

    private Mono<ClientRegistration> getClientRegistration(String tenant) {
        return Mono.fromSupplier(() -> {
            var provider = providerResolver.getProviders(tenant);
            var registration = registrationResolver.getRegistration(tenant);
            var properties = new SimpleOAuth2Properties(provider, Collections.singletonMap(tenant, registration));
            var clientRegistrations = OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(properties);
            return clientRegistrations.get(tenant);
        }).cache(Duration.of(1, ChronoUnit.MINUTES)); // TODO: Make cache time configurable and increase default.
    }

    private String replaceTenant(String templateString, String tenant) {
        if (templateString == null) {
            return null;
        }
        var uriVariables = Collections.singletonMap("tenant", tenant);
        return UriComponentsBuilder.fromUriString(templateString).buildAndExpand(uriVariables).toUriString();
    }
}
