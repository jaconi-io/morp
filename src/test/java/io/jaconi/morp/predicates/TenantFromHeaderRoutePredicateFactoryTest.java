package io.jaconi.morp.predicates;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import static org.assertj.core.api.Assertions.*;

class TenantFromHeaderRoutePredicateFactoryTest {

    @Test
    void shortcutFieldOrder() {
        var factory = new TenantFromHeaderRoutePredicateFactory();
        assertThat(factory.shortcutFieldOrder()).containsExactly("header", "pattern");
    }

    @Test
    void applyMatch() {
        var factory = new TenantFromHeaderRoutePredicateFactory();
        var predicate = factory.apply(config("X-Tenant-ID", "{tenant}"));

        var request = MockServerHttpRequest
                .get("/test")
                .header("X-Tenant-ID", "foo")
                .build();

        var exchange = new MockServerWebExchange.Builder(request).build();
        assertThat(predicate.test(exchange)).isTrue();
        assertThat(ServerWebExchangeUtils.getUriTemplateVariables(exchange)).contains(entry("tenant", "foo"));
    }

    @Test
    void applyMissingHeader() {
        var factory = new TenantFromHeaderRoutePredicateFactory();
        var predicate = factory.apply(config("X-Tenant-ID", "{tenant}"));

        var request = MockServerHttpRequest
                .get("/test")
                .build();

        var exchange = new MockServerWebExchange.Builder(request).build();
        assertThat(predicate.test(exchange)).isFalse();
        assertThat(ServerWebExchangeUtils.getUriTemplateVariables(exchange)).doesNotContainKey("tenant");
    }

    @Test
    void applyNoMatch() {
        var factory = new TenantFromHeaderRoutePredicateFactory();
        var predicate = factory.apply(config("X-Tenant-ID", "tenant-{tenant}"));

        var request = MockServerHttpRequest
                .get("/test")
                .header("X-Tenant-ID", "foo")
                .build();

        var exchange = new MockServerWebExchange.Builder(request).build();
        assertThat(predicate.test(exchange)).isFalse();
        assertThat(ServerWebExchangeUtils.getUriTemplateVariables(exchange)).doesNotContainKey("tenant");
    }

    @Test
    void applyIllegalPattern() {
        assertThatThrownBy(() -> config("X-Tenant", "{not-tenant}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pattern must contain \"{tenant}\"");
    }

    private TenantFromHeaderRoutePredicateFactory.Config config(String header, String pattern) {
        var config = new TenantFromHeaderRoutePredicateFactory.Config();
        config.setHeader(header);
        config.setPattern(pattern);
        return config;
    }
}
