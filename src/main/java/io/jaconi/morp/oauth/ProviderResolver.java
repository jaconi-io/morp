package io.jaconi.morp.oauth;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Get all configured {@link OAuth2ClientProperties.Provider} instances. Replace all occurrences of "{tenant}" by the
 * actual tenant ID.
 */
@Component
@AllArgsConstructor
public class ProviderResolver {
    private final OAuth2ClientProperties properties;

    Map<String, OAuth2ClientProperties.Provider> getProviders(String tenant) {
        if (properties.getProvider() == null) {
            return null;
        }

        var result = new HashMap<String, OAuth2ClientProperties.Provider>();
        for (var entry : properties.getProvider().entrySet()) {
            var key = entry.getKey();
            var template = entry.getValue();

            var provider = new OAuth2ClientProperties.Provider();
            provider.setAuthorizationUri(replaceTenant(template.getAuthorizationUri(), tenant));
            provider.setIssuerUri(replaceTenant(template.getIssuerUri(), tenant));
            provider.setJwkSetUri(replaceTenant(template.getJwkSetUri(), tenant));
            provider.setTokenUri(replaceTenant(template.getTokenUri(), tenant));
            provider.setUserInfoUri(replaceTenant(template.getUserInfoUri(), tenant));
            provider.setUserInfoAuthenticationMethod(replaceTenant(template.getUserInfoAuthenticationMethod(), tenant));
            provider.setUserNameAttribute(replaceTenant(template.getUserInfoAuthenticationMethod(), tenant));
            result.put(key, provider);
        }

        return result;
    }

    private String replaceTenant(String templateString, String tenant) {
        if (templateString == null) {
            return null;
        }
        var uriVariables = Collections.singletonMap("tenant", tenant);
        return UriComponentsBuilder.fromUriString(templateString).buildAndExpand(uriVariables).toUriString();
    }
}
