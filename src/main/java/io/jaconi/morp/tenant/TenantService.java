package io.jaconi.morp.tenant;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TenantService {

    @NonNull
    private final TenantProperties tenantProperties;

    public OAuth2ClientProperties.Registration getRegistration(String tenant) {
        Assert.notNull(tenant, "tenant cannot be null");

        if (tenantProperties.tenant() == null || !tenantProperties.tenant().containsKey(tenant)) {
            return null;
        }

        return tenantProperties.tenant().get(tenant).registration();
    }

    public String getRegistrationId(String tenant) {
        Assert.notNull(tenant, "tenant cannot be null");

        if (tenantProperties.tenant() == null || !tenantProperties.tenant().containsKey(tenant) || tenantProperties.tenant().get(tenant).oauth2ClientRegistration() == null) {
            return tenantProperties.defaultOauth2ClientRegistration();
        }

        return tenantProperties.tenant().get(tenant).oauth2ClientRegistration();
    }

    public Map<String, List<String>> getClaimConstraints(String tenant) {
        Assert.notNull(tenant, "tenant cannot be null");
        TenantProperties.TenantSettings tenantSettings = tenantProperties.tenant().get(tenant);
        if (tenantSettings == null
                || tenantSettings.claimConstraints() == null
        ) {
            return Collections.emptyMap();
        }
        return tenantSettings.claimConstraints();
    }
}
