package io.jaconi.morp.oauth;

import lombok.NonNull;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Simple POJO-variant of {@link OAuth2ClientProperties}.
 */
@ConfigurationProperties(prefix = "morp.oauth2-client")
@Primary
@Component
@Validated
class SimpleOAuth2Properties extends OAuth2ClientProperties {

    public SimpleOAuth2Properties(@NotNull Map<String, Provider> provider, @NotNull Map<String, Registration> registration) {
        getProvider().putAll(provider);
        getRegistration().putAll(registration);
    }
}
