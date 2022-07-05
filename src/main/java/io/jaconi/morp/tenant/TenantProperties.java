package io.jaconi.morp.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Map;

@ConfigurationProperties(prefix = "jaconi.oidc-proxy")
public record TenantProperties(Map<String, TenantSettings> tenant) {
    public record TenantSettings(ClientRegistration registration, Map<String, String> claimConstraints) {
        public record ClientRegistration(String clientId, String clientSecret) {
        }
    }
}
