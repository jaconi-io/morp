package io.jaconi.morp.oauth;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Simple POJO-variant of {@link OAuth2ClientProperties}.
 */
@ConfigurationProperties(prefix = "morp.oauth2-client")
@Primary
@Component
@Validated
class SimpleOAuth2Properties extends OAuth2ClientProperties {

    public SimpleOAuth2Properties() {
        super();
    }

    public SimpleOAuth2Properties(@NotNull Map<String, Provider> provider, @NotNull Map<String, Registration> registration) {
        getProvider().putAll(provider);
        getRegistration().putAll(registration);
    }

    public SimpleOAuth2Properties(String tenant, ClientRegistrationSource clientRegistrationSource) {
        this(clientRegistrationSource.getProvider() == null ? Map.of() : Map.of(clientRegistrationSource.getRegistration().getProvider(), clientRegistrationSource.getProvider()),
                Map.of(tenant, clientRegistrationSource.getRegistration()));
    }

    @Override
    public void afterPropertiesSet() {
        // Skip client ID validation here
    }

}
