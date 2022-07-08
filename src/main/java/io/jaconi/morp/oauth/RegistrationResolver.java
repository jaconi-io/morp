package io.jaconi.morp.oauth;

import io.jaconi.morp.tenant.TenantProperties;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.PropertyMapper;
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
        var merged = new OAuth2ClientProperties.Registration();

        if (global == null) {
            BeanUtils.copyProperties(tenantSpecific, merged);
            return merged;
        }

        BeanUtils.copyProperties(global, merged);
        if (tenantSpecific != null) {
            var map = PropertyMapper.get().alwaysApplyingWhenNonNull();
            map.from(tenantSpecific::getAuthorizationGrantType).to(merged::setAuthorizationGrantType);
            map.from(tenantSpecific::getProvider).to(merged::setProvider);
            map.from(tenantSpecific::getClientId).to(merged::setClientId);
            map.from(tenantSpecific::getClientAuthenticationMethod).to(merged::setClientAuthenticationMethod);
            map.from(tenantSpecific::getClientName).to(merged::setClientName);
            map.from(tenantSpecific::getClientSecret).to(merged::setClientSecret);
            map.from(tenantSpecific::getRedirectUri).to(merged::setRedirectUri);
            map.from(tenantSpecific::getScope).to(merged::setScope);
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
