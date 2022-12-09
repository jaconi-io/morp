package io.jaconi.morp.tenant;

import io.micrometer.core.instrument.Tags;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.support.tagsprovider.GatewayTagsProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import static io.jaconi.morp.tenant.TenantExtractor.KEY;

@Component
@ConditionalOnProperty(prefix = "morp.metrics.tenantdimension", name = "enabled", havingValue = "true")
public class TenantGatewayTagsProvider implements GatewayTagsProvider {

    @Override
    public Tags apply(ServerWebExchange exchange) {
        return TenantExtractor.extractTenant(exchange.getAttributes())
                .map(t -> Tags.of(KEY, t))
                .orElse(Tags.empty());
    }
}
