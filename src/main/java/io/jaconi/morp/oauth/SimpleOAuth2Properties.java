package io.jaconi.morp.oauth;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Simple POJO-variant of {@link OAuth2ClientProperties}.
 */
@AllArgsConstructor
@ConfigurationProperties(prefix = "morp.oauth2-client")
@Primary
@Component
class SimpleOAuth2Properties extends OAuth2ClientProperties {
    private final Map<String, Provider> provider;
    private final Map<String, Registration> registration;

    @Override
    public Map<String, Provider> getProvider() {
        return provider;
    }

    @Override
    public Map<String, Registration> getRegistration() {
        return registration;
    }
}
