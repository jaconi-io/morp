package io.jaconi.morp.oauth;

import io.jaconi.morp.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;

/**
 * The registration resolver builds a unified registration from a global registration and a tenant-specific
 * registration.
 */
@Component
@RequiredArgsConstructor
class RegistrationResolver {
    private final OAuth2ClientProperties properties;

    private final TenantService tenantService;

    private static OAuth2ClientProperties.Registration merge(OAuth2ClientProperties.Registration global, OAuth2ClientProperties.Registration tenantSpecific) {
        var merged = new OAuth2ClientProperties.Registration();

        if (global == null) {
            if (tenantSpecific == null) {
                return null;
            }
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
        var tenantSpecificRegistration = tenantService.getRegistration(tenant);
        var registrationId = tenantService.getRegistrationId(tenant);
        var globalRegistration = getGlobalRegistration(registrationId);
        var merged = merge(globalRegistration, tenantSpecificRegistration);

        if (merged != null) {
            if (merged.getProvider() == null) {
                merged.setProvider(registrationId);
            }

            if (merged.getAuthorizationGrantType() == null) {
                merged.setAuthorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
            }

            if (merged.getRedirectUri() == null) {
                merged.setRedirectUri("{baseUrl}/{action}/oauth2/code/{registrationId}");
            }

            if (merged.getClientId() == null) {
                throw new IllegalStateException(String.format("Client ID for tenant %s cannot be null.", tenant));
            }
        }
        return merged;
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
