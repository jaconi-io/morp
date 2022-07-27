package io.jaconi.morp.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

/**
 * Get all configured {@link OAuth2ClientProperties.Provider} instances. Replace all occurrences of "{tenant}" by the
 * actual tenant ID.
 */
@Component
@RequiredArgsConstructor
public class ProviderResolver {
    private final OAuth2ClientProperties properties;

    OAuth2ClientProperties.Provider getProvider(String tenant, String providerId) {
        if (properties.getProvider() == null) {
            return null;
        }

        if (!properties.getProvider().containsKey(providerId)) {
            return null;
        }
        var template = properties.getProvider().get(providerId);
        var provider = new OAuth2ClientProperties.Provider();
        provider.setAuthorizationUri(replaceTenant(template.getAuthorizationUri(), tenant));
        provider.setIssuerUri(replaceTenant(template.getIssuerUri(), tenant));
        provider.setJwkSetUri(replaceTenant(template.getJwkSetUri(), tenant));
        provider.setTokenUri(replaceTenant(template.getTokenUri(), tenant));
        provider.setUserInfoUri(replaceTenant(template.getUserInfoUri(), tenant));
        provider.setUserInfoAuthenticationMethod(replaceTenant(template.getUserInfoAuthenticationMethod(), tenant));
        provider.setUserNameAttribute(replaceTenant(template.getUserNameAttribute(), tenant));

        return provider;
    }

    private String replaceTenant(String templateString, String tenant) {
        if (templateString == null) {
            return null;
        }
        var uriVariables = Collections.singletonMap("tenant", tenant);
        return UriComponentsBuilder.fromUriString(templateString).buildAndExpand(uriVariables).toUriString();
    }

}
