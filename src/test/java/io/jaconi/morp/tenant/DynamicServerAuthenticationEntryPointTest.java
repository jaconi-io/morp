package io.jaconi.morp.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.client.HttpServerErrorException;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DynamicServerAuthenticationEntryPointTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void commence() {
        var entryPoint = new DynamicServerAuthenticationEntryPoint(exchange -> "foo");
        var request = MockServerHttpRequest.get("/foo").build();
        var exchange = new MockServerWebExchange.Builder(request).build();

        entryPoint.commence(exchange, null).block();

        assertEquals(HttpStatus.FOUND, exchange.getResponse().getStatusCode());
        assertEquals(URI.create("/oauth2/authorization/foo"), exchange.getResponse().getHeaders().getLocation());
    }

    @Test
    void commenceNoTenant() {
        var entryPoint = new DynamicServerAuthenticationEntryPoint(exchange -> null);
        var request = MockServerHttpRequest.get("/foo").build();
        var exchange = new MockServerWebExchange.Builder(request).build();

        assertThrows(HttpServerErrorException.class, () -> entryPoint.commence(exchange, null).block());
    }
}
