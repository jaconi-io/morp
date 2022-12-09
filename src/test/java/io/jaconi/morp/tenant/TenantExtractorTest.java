package io.jaconi.morp.tenant;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TenantExtractorTest {

    @Test
    void extractFooTenant() {
        var request = MockServerHttpRequest.get("/foo").build();
        var exchange = new MockServerWebExchange.Builder(request).build();
        ServerWebExchangeUtils.putUriTemplateVariables(exchange, Map.of("tenant", "foo"));

        Optional<String> tenant = TenantExtractor.extractTenant(exchange.getAttributes());

        assertThat(tenant).contains("foo");
    }

    @Test
    void extractNoTenant() {
        var request = MockServerHttpRequest.get("/foo").build();
        var exchange = new MockServerWebExchange.Builder(request).build();
        ServerWebExchangeUtils.putUriTemplateVariables(exchange, Map.of());

        Optional<String> tenant = TenantExtractor.extractTenant(exchange.getAttributes());

        assertThat(tenant).isEmpty();
    }

}