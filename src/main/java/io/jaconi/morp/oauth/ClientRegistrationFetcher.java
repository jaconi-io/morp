package io.jaconi.morp.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientPropertiesMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientRegistrationFetcher {

    public ClientRegistration getRegistration(String tenant, ClientRegistrationSource clientRegistrationSource) {
        log.debug("Creating Client Registration for tenant '{}' from scratch.", tenant);
        var properties = new SimpleOAuth2Properties(tenant, clientRegistrationSource);
        var clientRegistrations = new OAuth2ClientPropertiesMapper(properties).asClientRegistrations();
        return withDefaults(clientRegistrations.get(tenant));
    }

    private static ClientRegistration withDefaults(ClientRegistration clientRegistration) {
        if (clientRegistration.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName() == null) {
            ClientRegistration.Builder builder = ClientRegistration.withClientRegistration(clientRegistration);
            builder.userNameAttributeName(IdTokenClaimNames.SUB);
            clientRegistration = builder.build();
        }
        return clientRegistration;
    }
}
