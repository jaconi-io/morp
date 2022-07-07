package io.jaconi.morp.oauth;

import io.jaconi.morp.tenant.TenantProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TenantAwareClientRegistrationRepository implements ReactiveClientRegistrationRepository {

    private final Map<String, Mono<ClientRegistration>> clientCache = new ConcurrentHashMap<>();

    private final Map<String, ClientRegistration> providerIdToClientRegistrationTemplate;

    private final TenantProperties tenantProperties;

    private final Oauth2ClientDiscoverer oauth2ClientDiscoverer;

    /**
     * Constructs an {@code InMemoryReactiveClientRegistrationRepository} using the
     * provided parameters.
     *
     * @param registrations the client registration(s)
     */
    public TenantAwareClientRegistrationRepository(List<ClientRegistration> registrations, TenantProperties tenantProperties, Oauth2ClientDiscoverer oauth2ClientDiscoverer) {
        this.providerIdToClientRegistrationTemplate = toConcurrentMap(registrations);
        this.tenantProperties = tenantProperties;
        this.oauth2ClientDiscoverer = oauth2ClientDiscoverer;
    }

    @Override
    public Mono<ClientRegistration> findByRegistrationId(String tenant) {
        return clientCache.computeIfAbsent(tenant, this::getClientRegistration);
    }

    private Mono<ClientRegistration> getClientRegistration(String tenant) {
        return Mono.just(oauth2ClientDiscoverer.discoverOauth2Client(tenant)).map(providerIdToClientRegistrationTemplate::get).map(c -> enrichClientRegistration(c, tenant)).cache(Duration.of(1, ChronoUnit.MINUTES));
    }

    private ClientRegistration enrichClientRegistration(ClientRegistration template, String tenant) {
        var builder = ClientRegistration.withClientRegistration(template).registrationId(tenant).userInfoUri(replaceTenant(template.getProviderDetails().getUserInfoEndpoint().getUri(), tenant)).tokenUri(replaceTenant(template.getProviderDetails().getTokenUri(), tenant)).authorizationUri(replaceTenant(template.getProviderDetails().getAuthorizationUri(), tenant)).jwkSetUri(replaceTenant(template.getProviderDetails().getJwkSetUri(), tenant)).issuerUri(replaceTenant(template.getProviderDetails().getIssuerUri(), tenant));
        if (tenantProperties.tenant().containsKey(tenant) && tenantProperties.tenant().get(tenant).registration() != null) {
            OAuth2ClientProperties.Registration registration = tenantProperties.tenant().get(tenant).registration();
            PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
            map.from(registration::getClientId).to(builder::clientId);
            map.from(registration::getClientSecret).to(builder::clientSecret);
        }
        return builder.build();
    }

    private String replaceTenant(String templateString, String tenant) {
        if (templateString == null) {
            return null;
        }
        var uriVariables = Collections.singletonMap("tenant", tenant);
        return UriComponentsBuilder.fromUriString(templateString).buildAndExpand(uriVariables).toUriString();
    }

    private static Map<String, ClientRegistration> toConcurrentMap(List<ClientRegistration> registrations) {
        Assert.notEmpty(registrations, "registrations cannot be null or empty");
        ConcurrentHashMap<String, ClientRegistration> result = new ConcurrentHashMap<>();
        for (ClientRegistration registration : registrations) {
            Assert.notNull(registration, "no registration can be null");
            if (result.containsKey(registration.getRegistrationId())) {
                throw new IllegalStateException(String.format("Duplicate key %s", registration.getRegistrationId()));
            }
            result.put(registration.getRegistrationId(), registration);
        }
        return result;
    }

}
