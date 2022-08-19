package io.jaconi.morp.predicates;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.HostRoutePredicateFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Component
public class TenantFromHostRoutePredicateFactory extends AbstractRoutePredicateFactory<TenantFromHostRoutePredicateFactory.Config> {
    private final HostRoutePredicateFactory hostRoutePredicateFactory;

    public TenantFromHostRoutePredicateFactory() {
        super(Config.class);
        this.hostRoutePredicateFactory = new HostRoutePredicateFactory();
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("tenant", "patterns");
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return serverWebExchange -> {
            // Mimic the default host route predicate. Skip if the host does not match.
            var hostRoutePredicate = hostRoutePredicateFactory.apply(new HostRoutePredicateFactory.Config().setPatterns(config.getPatterns()));
            if (!hostRoutePredicate.test(serverWebExchange)) {
                return false;
            }

            ServerWebExchangeUtils.putUriTemplateVariables(serverWebExchange, Map.of("tenant", config.getTenant()));
            return true;
        };
    }

    @Data
    @NoArgsConstructor
    public static class Config {
        private String tenant;
        private List<String> patterns;
    }
}
