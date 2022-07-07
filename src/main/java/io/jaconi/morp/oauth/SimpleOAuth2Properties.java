package io.jaconi.morp.oauth;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;

import java.util.Map;

/**
 * Simple POJO-variant of {@link OAuth2ClientProperties}.
 */
@AllArgsConstructor
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
