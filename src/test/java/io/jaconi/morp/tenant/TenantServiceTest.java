package io.jaconi.morp.tenant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class TenantServiceTest {

    private static OAuth2ClientProperties.Registration tenantRegistration() {
        OAuth2ClientProperties.Registration registration = new OAuth2ClientProperties.Registration();
        registration.setProvider("tenant");
        registration.setClientId("tenant1");
        registration.setClientSecret("secret1");
        registration.setAuthorizationGrantType("authorization_code");
        registration.setClientAuthenticationMethod("client_secret");
        return registration;
    }

    @Test
    void testGetRegistrationTenantNull() {
        TenantService tenantService = new TenantService(new TenantProperties(Map.of(), "default"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tenantService.getRegistration(null);
        });
    }

    @Test
    void testGetRegistrationNoMatchingTenant() {
        TenantService tenantService = new TenantService(new TenantProperties(Map.of(), "default"));
        OAuth2ClientProperties.Registration registration = tenantService.getRegistration("tenant1");
        assertNull(registration);
    }

    @Test
    void testGetRegistration() {
        OAuth2ClientProperties.Registration tenant1 = tenantRegistration();
        TenantService tenantService = new TenantService(
                new TenantProperties(Map.of("tenant1",
                        new TenantProperties.TenantSettings(tenant1, Map.of(), "tenant1")), "default"));
        OAuth2ClientProperties.Registration registration = tenantService.getRegistration("tenant1");

        assertNotNull(registration);
        assertSame(tenant1, registration);
    }

    @Test
    void testGetRegistrationIdTenantNull() {
        TenantService tenantService = new TenantService(new TenantProperties(Map.of(), "default"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tenantService.getRegistrationId(null);
        });
    }

    @Test
    void testGetRegistrationIdDefault() {
        TenantService tenantService = new TenantService(new TenantProperties(Map.of(), "default"));
        String registrationId = tenantService.getRegistrationId("tenant1");
        assertEquals("default", registrationId);
    }

    @Test
    void testGetRegistrationId() {
        OAuth2ClientProperties.Registration tenant1 = tenantRegistration();
        TenantService tenantService = new TenantService(
                new TenantProperties(Map.of("tenant1",
                        new TenantProperties.TenantSettings(tenant1, Map.of(), "tenantRegistration")), "default"));
        String registrationId = tenantService.getRegistrationId("tenant1");

        assertEquals("tenantRegistration", registrationId);
    }

    @Test
    void testGetClaimConstraintsTenantNull() {
        TenantService tenantService = new TenantService(new TenantProperties(Map.of(), "default"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tenantService.getClaimConstraints(null);
        });
    }

    @Test
    void testGetClaimConstraintsNoMatchingTenant() {
        TenantService tenantService = new TenantService(new TenantProperties(Map.of(), "default"));
        Map<String, String> constraints = tenantService.getClaimConstraints("tenant1");
        assertThat(constraints, anEmptyMap());
    }

    @Test
    void testGetClaimConstraints() {
        TenantService tenantService = new TenantService(
                new TenantProperties(Map.of("tenant1",
                        new TenantProperties.TenantSettings(null, Map.of("sub", "whatever"), "tenantRegistration")), "default"));

        Map<String, String> constraints = tenantService.getClaimConstraints("tenant1");
        assertThat(constraints, hasEntry("sub", "whatever"));
        assertThat(constraints, aMapWithSize(1));
    }

}
