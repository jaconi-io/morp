package io.jaconi.morp.oauth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProviderResolverTest {

    private static final String PREFIX = "http://example.com/";

    @Test
    void testEmpty() {
        OAuth2ClientProperties properties = new SimpleOAuth2Properties(Map.of(), Map.of());
        ProviderResolver providerResolver = new ProviderResolver(properties);

        Map<String, OAuth2ClientProperties.Provider> result = providerResolver.getProviders("tenant1");
        assertEquals(0, result.size());
    }

    @Test
    void testReplace() {
        OAuth2ClientProperties properties = new SimpleOAuth2Properties(Map.of("tenant1", buildProvider()), Map.of());
        ProviderResolver providerResolver = new ProviderResolver(properties);

        Map<String, OAuth2ClientProperties.Provider> result = providerResolver.getProviders("tenant1");
        assertEquals(1, result.size());

        OAuth2ClientProperties.Provider provider = result.values().iterator().next();

        assertEquals(PREFIX + "a/tenant1", provider.getAuthorizationUri());
        assertEquals(PREFIX + "i/tenant1", provider.getIssuerUri());
        assertEquals(PREFIX + "t/tenant1", provider.getTokenUri());
        assertEquals(PREFIX + "u/tenant1", provider.getUserInfoUri());
        assertEquals(PREFIX + "j/tenant1", provider.getJwkSetUri());
    }

    private static OAuth2ClientProperties.Provider buildProvider() {
        OAuth2ClientProperties.Provider provider = new OAuth2ClientProperties.Provider();
        provider.setAuthorizationUri(PREFIX + "a/{tenant}");
        provider.setIssuerUri(PREFIX + "i/{tenant}");
        provider.setTokenUri(PREFIX + "t/{tenant}");
        provider.setUserInfoUri(PREFIX + "u/{tenant}");
        provider.setJwkSetUri(PREFIX + "j/{tenant}");
        return provider;
    }

}