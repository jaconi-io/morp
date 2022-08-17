package io.jaconi.morp.predicates;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class TenantFromHostRoutePredicateFactoryTest {

    @Test
    void shortcutFieldOrder() {
        var factory = new TenantFromHostRoutePredicateFactory();
        assertThat(factory.shortcutFieldOrder()).containsExactly("tenant", "patterns");
    }

    @Test
    void applyMatch() {
        var factory = new TenantFromHostRoutePredicateFactory();
        var predicate = factory.apply(config("foo", List.of("match.example.com")));

        var request = MockServerHttpRequest
                .get("/test")
                .header("Host", "match.example.com")
                .build();

        var exchange = new MockServerWebExchange.Builder(request).build();
        assertThat(predicate.test(exchange)).isTrue();
        assertThat(ServerWebExchangeUtils.getUriTemplateVariables(exchange)).contains(entry("tenant", "foo"));
    }

    @Test
    void applyNoMatch() {
        var factory = new TenantFromHostRoutePredicateFactory();
        var predicate = factory.apply(config("foo", List.of("static.example.com")));

        var request = MockServerHttpRequest
                .get("/test")
                .header("Host", "mismatch.example.com")
                .build();

        var exchange = new MockServerWebExchange.Builder(request).build();
        assertThat(predicate.test(exchange)).isFalse();
        assertThat(ServerWebExchangeUtils.getUriTemplateVariables(exchange)).doesNotContainKey("tenant");
    }

    private TenantFromHostRoutePredicateFactory.Config config(String tenant, List<String> patterns) {
        var config = new TenantFromHostRoutePredicateFactory.Config();
        config.setTenant(tenant);
        config.setPatterns(patterns);
        return config;
    }
}
