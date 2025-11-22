package io.jaconi.morp.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientRegistrationFetcherTest {


    @Test
    void testDefaults() {
        ClientRegistrationSource clientRegistrationSource = new ClientRegistrationSource(buildProvider(), buildRegistration());

        ClientRegistrationFetcher clientRegistrationFetcher = new ClientRegistrationFetcher();

        ClientRegistration registration = clientRegistrationFetcher.getRegistration("tenant1", clientRegistrationSource);

        assertNotNull(registration);
        assertEquals(IdTokenClaimNames.SUB, registration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName());
    }

    private static OAuth2ClientProperties.Provider buildProvider() {
        OAuth2ClientProperties.Provider provider = new OAuth2ClientProperties.Provider();
        provider.setAuthorizationUri("a");
        provider.setTokenUri("t");
        provider.setUserInfoUri("u");
        provider.setJwkSetUri("j");
        return provider;
    }

    private static OAuth2ClientProperties.Registration buildRegistration() {
        OAuth2ClientProperties.Registration registration = new OAuth2ClientProperties.Registration();
        registration.setProvider("google");
        registration.setClientId("dummy");
        return registration;
    }

}