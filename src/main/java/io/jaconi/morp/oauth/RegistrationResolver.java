package io.jaconi.morp.oauth;

import io.jaconi.morp.tenant.TenantProperties;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.stereotype.Component;

/**
 * The registration resolver builds a unified registration from a global registration and a tenant-specific
 * registration.
 */
@Component
@AllArgsConstructor
class RegistrationResolver {
    private final MorpOAuth2ClientProperties properties;
    private final TenantProperties tenantProperties;

    private static OAuth2ClientProperties.Registration merge(OAuth2ClientProperties.Registration global, OAuth2ClientProperties.Registration tenantSpecific) {
        if (global == null) {
            return tenantSpecific;
        }

        if (tenantSpecific == null) {
            return global;
        }

        var merged = new OAuth2ClientProperties.Registration();
        if (tenantSpecific.getAuthorizationGrantType() == null) {
            merged.setAuthorizationGrantType(global.getAuthorizationGrantType());
        } else {
            merged.setAuthorizationGrantType(tenantSpecific.getAuthorizationGrantType());
        }

        if (tenantSpecific.getProvider() == null) {
            merged.setProvider(global.getProvider());
        } else {
            merged.setProvider(tenantSpecific.getProvider());
        }

        if (tenantSpecific.getClientId() == null) {
            merged.setClientId(global.getClientId());
        } else {
            merged.setClientId(tenantSpecific.getClientId());
        }

        if (tenantSpecific.getClientAuthenticationMethod() == null) {
            merged.setClientAuthenticationMethod(global.getClientAuthenticationMethod());
        } else {
            merged.setClientAuthenticationMethod(tenantSpecific.getClientAuthenticationMethod());
        }

        if (tenantSpecific.getClientName() == null) {
            merged.setClientName(global.getClientName());
        } else {
            merged.setClientName(tenantSpecific.getClientName());
        }

        if (tenantSpecific.getClientSecret() == null) {
            merged.setClientSecret(global.getClientSecret());
        } else {
            merged.setClientSecret(tenantSpecific.getClientSecret());
        }

        if (tenantSpecific.getRedirectUri() == null) {
            merged.setRedirectUri(global.getRedirectUri());
        } else {
            merged.setRedirectUri(tenantSpecific.getRedirectUri());
        }

        if (tenantSpecific.getScope() == null) {
            merged.setScope(global.getScope());
        } else {
            merged.setScope(tenantSpecific.getScope());
        }

        return merged;
    }

    public OAuth2ClientProperties.Registration getRegistration(String tenant) {
        var tenantSpecificRegistration = getTenantSpecificRegistration(tenant);
        var registrationId = getRegistrationId(tenant);
        var globalRegistration = getGlobalRegistration(registrationId);
        var merged = merge(globalRegistration, tenantSpecificRegistration);

        if (merged != null && merged.getProvider() == null) {
            merged.setProvider(registrationId);
        }

        return merged;
    }

    private OAuth2ClientProperties.Registration getTenantSpecificRegistration(String tenant) {
        if (tenantProperties == null) {
            return null;
        }

        if (tenantProperties.tenant() == null || !tenantProperties.tenant().containsKey(tenant)) {
            return null;
        }

        return tenantProperties.tenant().get(tenant).registration();
    }

    private String getRegistrationId(String tenant) {
        if (tenantProperties == null) {
            return null;
        }

        if (tenantProperties.tenant() == null || !tenantProperties.tenant().containsKey(tenant) || tenantProperties.tenant().get(tenant).oauth2ClientRegistration() == null) {
            return tenantProperties.defaultOauth2ClientRegistration();
        }

        return tenantProperties.tenant().get(tenant).oauth2ClientRegistration();
    }

    private OAuth2ClientProperties.Registration getGlobalRegistration(String registrationId) {
        if (properties == null) {
            return null;
        }

        if (properties.getRegistration() == null || !properties.getRegistration().containsKey(registrationId)) {
            return null;
        }

        return properties.getRegistration().get(registrationId);
    }
}
