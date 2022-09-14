package io.jaconi.morp.tenant;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.actuate.metrics.web.reactive.server.WebFluxTagsContributor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import static io.jaconi.morp.tenant.TenantExtractor.KEY;

@Component
@ConditionalOnProperty(prefix = "morp.metrics.tenantdimension", name = "enabled", havingValue = "true")
public class TenantWebFluxTagsContributor implements WebFluxTagsContributor {

    @Override
    public Iterable<Tag> httpRequestTags(ServerWebExchange exchange, Throwable ex) {
        return TenantExtractor.extractTenant(exchange)
                .map(t -> Tags.of(KEY, t))
                .orElse(Tags.empty());
    }
}
