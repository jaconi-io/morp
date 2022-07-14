package io.jaconi.morp.oauth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
        cacheManager.getCache(TenantAwareClientRegistrationRepository.FALLBACK).clear();
    }

    @Test
    void testFresh() {
        doReturn(clientRegistration).when(clientRegistrationFetcher).getRegistration(eq(TENANT));
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(r -> {
                    assertNotNull(r);
                    verifyCache();
                })
                .verifyComplete();

        // Client built once
        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT));
    }

    @Test
    void testCached() {
        doReturn(clientRegistration).when(clientRegistrationFetcher).getRegistration(eq(TENANT));
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(r -> {
                    assertNotNull(r);
                    // Result returned from cache
                    verifyCache();
                })
                .verifyComplete();

        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT));

        doThrow(RuntimeException.class).when(clientRegistrationFetcher).getRegistration(eq(TENANT));
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

        // Client not built again (taken from cache)
        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT));
    }

    @Test
    void testFallback() {
        doReturn(clientRegistration).when(clientRegistrationFetcher).getRegistration(eq(TENANT));
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(r -> {
                    assertNotNull(r);
                    // Result returned from cache
                    verifyCache();
                })
                .verifyComplete();

        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT));

        // Throw exception when building client
        doThrow(RuntimeException.class).when(clientRegistrationFetcher).getRegistration(eq(TENANT));
        cacheManager.getCache(TenantAwareClientRegistrationRepository.REGISTRATIONS).clear();

        // Client returned from fallback cache
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();

        // Method called again (exception thrown)
        verify(clientRegistrationFetcher, times(2)).getRegistration(eq(TENANT));
    }

    @Test
    void testErrorNoCache() {
        doThrow(RuntimeException.class).when(clientRegistrationFetcher).getRegistration(eq(TENANT));
        // Method throws error and nothing in cache -> No registration returned
        StepVerifier.create(tenantAwareClientRegistrationRepository.findByRegistrationId(TENANT))
                .verifyComplete();
        verify(clientRegistrationFetcher, times(1)).getRegistration(eq(TENANT));
    }

    void verifyCache() {
        assertNotNull(cacheManager.getCache(TenantAwareClientRegistrationRepository.REGISTRATIONS).get(TENANT));
        assertNotNull(cacheManager.getCache(TenantAwareClientRegistrationRepository.FALLBACK).get(TENANT));
    }

}