package io.jaconi.morp.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "jaconi.oidc-proxy")
public record TenantCredentials(Map<String, ClientCredentials> tenantCredentials) {

    public record ClientCredentials(String clientId, String clientSecret) {
    }
}
