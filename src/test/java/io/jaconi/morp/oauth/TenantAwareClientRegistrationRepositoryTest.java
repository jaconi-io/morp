package io.jaconi.morp.oauth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.aot.DisabledInAotMode;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisabledInAotMode
class TenantAwareClientRegistrationRepositoryTest {

    private static final String TENANT = "tenant1";

    @Autowired
    private TenantAwareClientRegistrationRepository tenantAwareClientRegistrationRepository;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private ClientRegistrationFetcher clientRegistrationFetcher;

    @MockBean
    private RegistrationResolver registrationResolver;

    @MockBean
    private ProviderResolver providerResolver;

    private ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(TENANT)
            .clientId("id")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("redirect")
            .authorizationUri("auth")
            .tokenUri("token")
            .build();

    @BeforeEach
    void clearCaches() {
        cacheManager.getCache(TenantAwareClientRegistrationRepository.REGISTRATIONS).clear();
        cacheManager.getCache(TenantAwareClientRegistrationRepository.SOURCE_HASHES).clear();
        when(providerResolver.getProvider(anyString(), anyString())).thenReturn(buildProvider("i"));
        when(registrationResolver.getRegistration(anyString())).thenReturn(buildRegistration(TENANT));
        when(clientRegistrationFetcher.getRegistration(eq(TENANT), any())).thenReturn(clientRegistration);
    }

    @Test
    void testFresh() {
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(r -> {
                    assertNotNull(r);
                    verifyCache();
                })
                .verifyComplete();

        // Client built once
        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT), any());
    }

    @Test
    void testCached() {
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(r -> {
                    assertNotNull(r);
                    // Result returned from cache
                    verifyCache();
                })
                .verifyComplete();

        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT), any());

        when(clientRegistrationFetcher.getRegistration(eq(TENANT), any())).thenThrow(RuntimeException.class);
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

        // Client not built again (taken from cache)
        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT), any());
    }

    @Test
    void testChange() {
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(r -> {
                    assertNotNull(r);
                    // Result put in cache
                    verifyCache();
                })
                .verifyComplete();

        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT), any());

        // Provider changes
        when(providerResolver.getProvider(anyString(), anyString())).thenReturn(buildProvider("newIssuer"));

        // Client rebuilt
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

        // Method called again
        verify(clientRegistrationFetcher, times(2)).getRegistration(eq(TENANT), any());
    }

    @Test
    void testChangeButError() {
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(r -> {
                    assertNotNull(r);
                    // Result put in cache
                    verifyCache();
                })
                .verifyComplete();

        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT), any());

        // Provider changes
        when(providerResolver.getProvider(anyString(), anyString())).thenReturn(buildProvider("newIssuer"));
        // But method returns error
        when(clientRegistrationFetcher.getRegistration(eq(TENANT), any())).thenThrow(RuntimeException.class);

        // Client returned from cache
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

        // Method called again
        verify(clientRegistrationFetcher, times(2)).getRegistration(eq(TENANT), any());
    }

    @Test
    void testErrorNoCache() {
        when(clientRegistrationFetcher.getRegistration(eq(TENANT), any())).thenThrow(RuntimeException.class);
        // Method throws error and nothing in cache -> No registration returned
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .verifyComplete();
        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT), any());
    }

    void verifyCache() {
        assertNotNull(cacheManager.getCache(TenantAwareClientRegistrationRepository.REGISTRATIONS).get(TENANT));
        assertNotNull(cacheManager.getCache(TenantAwareClientRegistrationRepository.SOURCE_HASHES).get(TENANT));
    }

    private static OAuth2ClientProperties.Provider buildProvider(String issuer) {
        OAuth2ClientProperties.Provider provider = new OAuth2ClientProperties.Provider();
        provider.setAuthorizationUri("a");
        provider.setIssuerUri(issuer);
        provider.setTokenUri("t");
        provider.setUserInfoUri("u");
        provider.setJwkSetUri("j");
        return provider;
    }

    private static OAuth2ClientProperties.Registration buildRegistration(String provider) {
        OAuth2ClientProperties.Registration registration = new OAuth2ClientProperties.Registration();
        registration.setProvider(provider);
        return registration;
    }

}