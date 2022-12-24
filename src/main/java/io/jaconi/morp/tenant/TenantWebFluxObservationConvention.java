package io.jaconi.morp.tenant;

import javax.annotation.Nonnull;
import io.micrometer.common.KeyValues;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.server.reactive.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import org.springframework.stereotype.Component;

import static io.jaconi.morp.tenant.TenantExtractor.KEY;

@Component
@ConditionalOnProperty(prefix = "morp.metrics.tenantdimension", name = "enabled", havingValue = "true")
public class TenantWebFluxObservationConvention extends DefaultServerRequestObservationConvention {

    @Override
    @Nonnull
    public KeyValues getLowCardinalityKeyValues(ServerRequestObservationContext context) {
        return super.getLowCardinalityKeyValues(context).and(tenant(context));
    }

    private KeyValues tenant(ServerRequestObservationContext context) {
        return TenantExtractor.extractTenant(context.getAttributes())
                .map(t -> KeyValues.of(KEY, t))
                .orElse(KeyValues.empty());
    }
}
