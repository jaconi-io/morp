package io.jaconi.morp.tenant;

import io.micrometer.common.KeyValues;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.observation.reactive.ServerRequestObservationContext;
import org.springframework.http.observation.reactive.ServerRequestObservationConvention;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

import static io.jaconi.morp.tenant.TenantExtractor.KEY;

@Component
@ConditionalOnProperty(prefix = "morp.metrics.tenantdimension", name = "enabled", havingValue = "true")
public class TenantWebFluxObservationConvention implements ServerRequestObservationConvention {

    @Override
    @Nonnull
    public KeyValues getHighCardinalityKeyValues(ServerRequestObservationContext context) {
        return TenantExtractor.extractTenant(context.getServerWebExchange())
                .map(t -> KeyValues.of(KEY, t))
                .orElse(KeyValues.empty());
    }
}
