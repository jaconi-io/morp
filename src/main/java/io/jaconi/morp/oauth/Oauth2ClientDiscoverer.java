package io.jaconi.morp.oauth;

import io.jaconi.morp.tenant.TenantProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Oauth2ClientDiscoverer {

    @Autowired
    private final TenantProperties tenantProperties;

    public String discoverOauth2Client(String tenant) {
        if (!tenantProperties.tenant().containsKey(tenant) || tenantProperties.tenant().get(tenant).oauth2ClientRegistration() == null) {
            return tenantProperties.defaultOauth2ClientRegistration();
        }
        return tenantProperties.tenant().get(tenant).oauth2ClientRegistration();
    }

}
