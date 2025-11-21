package io.jaconi.morp.oauth;

import io.jaconi.morp.tenant.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@DisabledInAotMode
class RegistrationResolverTest {

    @Autowired
    RegistrationResolver registrationResolver;

    @MockitoBean
	OAuth2ClientProperties properties;

    @MockitoBean
    TenantService tenantService;

    private static OAuth2ClientProperties.Registration tenantRegistration() {
        OAuth2ClientProperties.Registration registration = new OAuth2ClientProperties.Registration();
        registration.setProvider("tenant");
        registration.setClientId("tenant1");
        registration.setClientSecret("secret1");
        registration.setAuthorizationGrantType("authorization_code");
        registration.setClientAuthenticationMethod("client_secret");
        return registration;
    }

    private static OAuth2ClientProperties.Registration globalRegistration() {
        OAuth2ClientProperties.Registration registration = new OAuth2ClientProperties.Registration();
        registration.setProvider("global");
        registration.setClientId("globalclient");
        registration.setClientSecret("globalsecret");
        registration.setAuthorizationGrantType("authorization_code");
        registration.setClientName("Global");
        registration.setClientAuthenticationMethod("client_secret");
        registration.setScope(Set.of("openid", "profile"));
        registration.setRedirectUri("/redirect/global");
        return registration;
    }

    private static OAuth2ClientProperties.Registration noDefaultsRegistration() {
        OAuth2ClientProperties.Registration registration = new OAuth2ClientProperties.Registration();
        registration.setProvider("global");
        registration.setClientId("globalclient");
        registration.setClientSecret("globalsecret");
        registration.setClientName("Global");
        registration.setClientAuthenticationMethod("client_secret");
        registration.setScope(Set.of("openid", "profile"));
        // Some values not set
        registration.setRedirectUri(null);
        registration.setAuthorizationGrantType(null);
        return registration;
    }

    private static OAuth2ClientProperties.Registration noClientIdRegistration() {
        OAuth2ClientProperties.Registration registration = new OAuth2ClientProperties.Registration();
        registration.setProvider("global");
        registration.setClientId(null);
        return registration;
    }

    @Test
    void testNoRegistrations() {
        OAuth2ClientProperties.Registration registration = registrationResolver.getRegistration("tenant1");
        assertNull(registration);
    }

    @Test
    void testTenantOnly() {
        OAuth2ClientProperties.Registration tenant1 = tenantRegistration();
        when(tenantService.getRegistration(anyString())).thenReturn(tenant1);

        OAuth2ClientProperties.Registration registration = registrationResolver.getRegistration("tenant1");

        assertNotNull(registration);
        assertNotSame(tenant1, registration);
        assertEquals(tenant1.getAuthorizationGrantType(), registration.getAuthorizationGrantType());
        assertEquals(tenant1.getClientId(), registration.getClientId());
        assertEquals(tenant1.getClientName(), registration.getClientName());
        assertEquals(tenant1.getClientSecret(), registration.getClientSecret());
        assertEquals(tenant1.getClientAuthenticationMethod(), registration.getClientAuthenticationMethod());
        assertEquals(tenant1.getScope(), registration.getScope());
    }

    @Test
    void testOnlyGlobal() {
        OAuth2ClientProperties.Registration global = globalRegistration();
        when(tenantService.getRegistrationId(anyString())).thenReturn("default");
        when(properties.getRegistration()).thenReturn(Map.of("default", global));

        OAuth2ClientProperties.Registration registration = registrationResolver.getRegistration("tenant1");

        assertNotNull(registration);
        assertNotSame(global, registration);
        assertEquals(global.getAuthorizationGrantType(), registration.getAuthorizationGrantType());
        assertEquals(global.getClientId(), registration.getClientId());
        assertEquals(global.getClientName(), registration.getClientName());
        assertEquals(global.getClientSecret(), registration.getClientSecret());
        assertEquals(global.getClientAuthenticationMethod(), registration.getClientAuthenticationMethod());
        assertEquals(global.getRedirectUri(), registration.getRedirectUri());
        assertEquals(global.getScope(), registration.getScope());
    }

    @Test
    void testMerge() {
        OAuth2ClientProperties.Registration global = globalRegistration();
        OAuth2ClientProperties.Registration tenant1 = tenantRegistration();
        when(tenantService.getRegistration(anyString())).thenReturn(tenant1);
        when(tenantService.getRegistrationId(anyString())).thenReturn("tenant1");
        when(properties.getRegistration()).thenReturn(Map.of("tenant1", global));

        OAuth2ClientProperties.Registration registration = registrationResolver.getRegistration("tenant1");

        assertNotNull(registration);
        assertNotSame(global, registration);
        assertNotSame(tenant1, registration);
        assertEquals(tenant1.getAuthorizationGrantType(), registration.getAuthorizationGrantType());
        assertEquals(tenant1.getClientId(), registration.getClientId());
        assertEquals(global.getClientName(), registration.getClientName());
        assertEquals(tenant1.getClientSecret(), registration.getClientSecret());
        assertEquals(tenant1.getClientAuthenticationMethod(), registration.getClientAuthenticationMethod());
        assertEquals(global.getRedirectUri(), registration.getRedirectUri());
        assertEquals(global.getScope(), registration.getScope());
    }

    @Test
    void testDefaults() {
        OAuth2ClientProperties.Registration noDefaults = noDefaultsRegistration();
        when(tenantService.getRegistration(anyString())).thenReturn(noDefaults);
        when(tenantService.getRegistrationId(anyString())).thenReturn("tenant1");
        when(properties.getRegistration()).thenReturn(Map.of("tenant1", noDefaults));

        OAuth2ClientProperties.Registration registration = registrationResolver.getRegistration("tenant1");

        assertNotNull(registration);
        assertEquals(AuthorizationGrantType.AUTHORIZATION_CODE.getValue(), registration.getAuthorizationGrantType());
        assertEquals("{baseUrl}/{action}/oauth2/code/{registrationId}", registration.getRedirectUri());
    }

    @Test
    void testNoClientId() {
        OAuth2ClientProperties.Registration noClientId = noClientIdRegistration();
        when(tenantService.getRegistration(anyString())).thenReturn(noClientId);
        when(tenantService.getRegistrationId(anyString())).thenReturn("tenant1");
        when(properties.getRegistration()).thenReturn(Map.of("tenant1", noClientId));


        assertThrows(IllegalStateException.class, () -> {
            registrationResolver.getRegistration("tenant1");
        });
    }

}