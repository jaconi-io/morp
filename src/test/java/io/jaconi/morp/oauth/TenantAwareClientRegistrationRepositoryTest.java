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
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
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
    private void clearCaches() {
        cacheManager.getCache(TenantAwareClientRegistrationRepository.REGISTRATIONS).clear();
        cacheManager.getCache(TenantAwareClientRegistrationRepository.SOURCE_HASHES).clear();
    }

    @Test
    void testFresh() {
        doReturn(buildProvider("i")).when(providerResolver).getProvider(anyString(), anyString());
        doReturn(buildRegistration(TENANT)).when(registrationResolver).getRegistration(anyString());
        doReturn(clientRegistration).when(clientRegistrationFetcher).getRegistration(eq(TENANT), any());
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
        doReturn(buildProvider("i")).when(providerResolver).getProvider(anyString(), anyString());
        doReturn(buildRegistration(TENANT)).when(registrationResolver).getRegistration(anyString());
        doReturn(clientRegistration).when(clientRegistrationFetcher).getRegistration(eq(TENANT), any());
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(r -> {
                    assertNotNull(r);
                    // Result returned from cache
                    verifyCache();
                })
                .verifyComplete();

        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT), any());

        doThrow(RuntimeException.class).when(clientRegistrationFetcher).getRegistration(eq(TENANT), any());
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

        // Client not built again (taken from cache)
        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT), any());
    }

    @Test
    void testChange() {
        doReturn(buildProvider("i")).when(providerResolver).getProvider(anyString(), anyString());
        doReturn(buildRegistration(TENANT)).when(registrationResolver).getRegistration(anyString());
        doReturn(clientRegistration).when(clientRegistrationFetcher).getRegistration(eq(TENANT), any());
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(r -> {
                    assertNotNull(r);
                    // Result put in cache
                    verifyCache();
                })
                .verifyComplete();

        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT), any());

        // Provider changes
        doReturn(buildProvider("newIssuer")).when(providerResolver).getProvider(anyString(), anyString());

        // Client rebuilt
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

        // Method called again
        verify(clientRegistrationFetcher, times(2)).getRegistration(eq(TENANT), any());
    }

    @Test
    void testChangeButError() {
        doReturn(buildProvider("i")).when(providerResolver).getProvider(anyString(), anyString());
        doReturn(buildRegistration(TENANT)).when(registrationResolver).getRegistration(anyString());
        doReturn(clientRegistration).when(clientRegistrationFetcher).getRegistration(eq(TENANT), any());
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(r -> {
                    assertNotNull(r);
                    // Result put in cache
                    verifyCache();
                })
                .verifyComplete();

        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT), any());

        // Provider changes
        doReturn(buildProvider("newIssuer")).when(providerResolver).getProvider(anyString(), anyString());
        // But method returns error
        doThrow(RuntimeException.class).when(clientRegistrationFetcher).getRegistration(eq(TENANT), any());

        // Client returned from cache
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

        // Method called again
        verify(clientRegistrationFetcher, times(2)).getRegistration(eq(TENANT), any());
    }

    @Test
    void testErrorNoCache() {
        doReturn(buildProvider("i")).when(providerResolver).getProvider(anyString(), anyString());
        doReturn(buildRegistration(TENANT)).when(registrationResolver).getRegistration(anyString());
        doThrow(RuntimeException.class).when(clientRegistrationFetcher).getRegistration(eq(TENANT), any());
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