package io.jaconi.morp.oauth;

import io.jaconi.morp.idp.IDPMapper;
import io.jaconi.morp.tenant.TenantCredentials;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TenantAwareClientRegistrationRepository
        implements ReactiveClientRegistrationRepository, Iterable<ClientRegistration> {

    private final Map<String, ClientRegistration> providerIdToClientRegistrationTemplate;

    private final IDPMapper idpMapper;

    private final TenantCredentials tenantCredentials;

    /**
     * Constructs an {@code InMemoryReactiveClientRegistrationRepository} using the
     * provided parameters.
     *
     * @param registrations the client registration(s)
     */
    public TenantAwareClientRegistrationRepository(List<ClientRegistration> registrations, IDPMapper idpMapper, TenantCredentials tenantCredentials) {
        this.providerIdToClientRegistrationTemplate = toConcurrentMap(registrations);
        this.idpMapper = idpMapper;
        this.tenantCredentials = tenantCredentials;
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

    @Override
    public Mono<ClientRegistration> findByRegistrationId(String tenant) {
        return idpMapper.fromTenant(tenant)
                .map(providerIdToClientRegistrationTemplate::get)
                .map(c -> enrichClientRegistration(c, tenant));
    }

    private ClientRegistration enrichClientRegistration(ClientRegistration template, String tenant) {
        var builder = ClientRegistration.withClientRegistration(template)
                .registrationId(tenant)
                .userInfoUri(replaceTenant(template.getProviderDetails().getUserInfoEndpoint().getUri(), tenant))
                .tokenUri(replaceTenant(template.getProviderDetails().getTokenUri(), tenant))
                .authorizationUri(replaceTenant(template.getProviderDetails().getAuthorizationUri(), tenant))
                .jwkSetUri(replaceTenant(template.getProviderDetails().getJwkSetUri(), tenant))
                .issuerUri(replaceTenant(template.getProviderDetails().getIssuerUri(), tenant));
        if (tenantCredentials.tenantCredentials().containsKey(tenant)) {
            TenantCredentials.ClientCredentials creds = tenantCredentials.tenantCredentials().get(tenant);
            builder.clientId(creds.clientId());
            builder.clientSecret(creds.clientSecret());
        }
        return builder.build();
    }

    private String replaceTenant(String templateString, String tenant) {
        if (templateString == null) {
            return null;
        }
        var uriVariables = Collections.singletonMap("tenant", tenant);
        return UriComponentsBuilder.fromUriString(templateString)
                .buildAndExpand(uriVariables)
                .toUriString();
    }

    /**
     * Returns an {@code Iterator} of {@link ClientRegistration}.
     *
     * @return an {@code Iterator<ClientRegistration>}
     */
    @Override
    public Iterator<ClientRegistration> iterator() {
        return this.providerIdToClientRegistrationTemplate.values().iterator();
    }

    public void addRegistration(ClientRegistration reg) {
        this.providerIdToClientRegistrationTemplate.put(reg.getRegistrationId(), reg);
    }

}
