package io.jaconi.morp.tenant;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "morp")
public record TenantProperties(Map<String, TenantSettings> tenant, String defaultOauth2ClientRegistration) {
    public record TenantSettings(OAuth2ClientProperties.Registration registration, Map<String, String> claimConstraints,
                                 String oauth2ClientRegistration) {
    }
}
