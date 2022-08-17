package io.jaconi.morp.predicates;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.cloud.gateway.handler.predicate.HostRoutePredicateFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Component
public class TenantFromHeaderRoutePredicateFactory extends AbstractRoutePredicateFactory<TenantFromHeaderRoutePredicateFactory.Config> {
    private static final PathMatcher PATH_MATCHER = new AntPathMatcher(".");

    public TenantFromHeaderRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("header", "pattern");
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return serverWebExchange -> {
            var values = serverWebExchange.getRequest().getHeaders().getOrDefault(config.getHeader(), Collections.emptyList());

            var pattern = config.getPattern();
            for (String value : values) {
                if (PATH_MATCHER.match(pattern, value)) {
                    ServerWebExchangeUtils.putUriTemplateVariables(serverWebExchange, PATH_MATCHER.extractUriTemplateVariables(pattern, value));
                    return true;
                }
            }

            return false;
        };
    }

    @Data
    @NoArgsConstructor
    public static class Config {
        private String header;
        private String pattern;

        public void setPattern(String pattern) {
            if (!pattern.contains("{tenant}")) {
                throw new IllegalArgumentException("pattern must contain \"{tenant}\"");
            }

            this.pattern = pattern;
        }
    }
}
