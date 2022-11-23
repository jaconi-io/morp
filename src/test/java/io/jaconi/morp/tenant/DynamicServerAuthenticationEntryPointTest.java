package io.jaconi.morp.tenant;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.client.HttpServerErrorException;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicServerAuthenticationEntryPointTest {

    @Test
    void commence() {
        var entryPoint = new DynamicServerAuthenticationEntryPoint();
        var request = MockServerHttpRequest.get("/foo").build();
        var exchange = new MockServerWebExchange.Builder(request).build();
        ServerWebExchangeUtils.putUriTemplateVariables(exchange, Map.of("tenant", "foo"));

        entryPoint.commence(exchange, null).block();

        assertEquals(HttpStatus.FOUND, exchange.getResponse().getStatusCode());
        assertEquals(URI.create("/oauth2/authorization/foo"), exchange.getResponse().getHeaders().getLocation());
    }

    @Test
    void commenceNoTenant() {
        var entryPoint = new DynamicServerAuthenticationEntryPoint();
        var request = MockServerHttpRequest.get("/foo").build();
        var exchange = new MockServerWebExchange.Builder(request).build();
        ServerWebExchangeUtils.putUriTemplateVariables(exchange, Map.of());

        StepVerifier.create(entryPoint.commence(exchange, null))
                .expectError(HttpServerErrorException.class)
                .verify();
    }
}
