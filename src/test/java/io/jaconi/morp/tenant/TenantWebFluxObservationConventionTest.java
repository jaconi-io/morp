package io.jaconi.morp.tenant;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.observation.reactive.ServerRequestObservationContext;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TenantWebFluxObservationConventionTest {

    @Test
    void fooTenant() {
        var request = MockServerHttpRequest.get("/foo").build();
        var exchange = new MockServerWebExchange.Builder(request).build();
        var context = new ServerRequestObservationContext(exchange);
        ServerWebExchangeUtils.putUriTemplateVariables(exchange, Map.of("tenant", "foo"));

        TenantWebFluxObservationConvention tenantWebFluxObservationConvention = new TenantWebFluxObservationConvention();

        KeyValues keyValues = tenantWebFluxObservationConvention.getHighCardinalityKeyValues(context);

        assertThat(keyValues).containsExactly(KeyValue.of("tenant", "foo"));
    }

    @Test
    void noTenant() {
        var request = MockServerHttpRequest.get("/foo").build();
        var exchange = new MockServerWebExchange.Builder(request).build();
        var context = new ServerRequestObservationContext(exchange);
        ServerWebExchangeUtils.putUriTemplateVariables(exchange, Map.of());

        TenantWebFluxObservationConvention tenantWebFluxObservationConvention = new TenantWebFluxObservationConvention();

        KeyValues keyValues = tenantWebFluxObservationConvention.getHighCardinalityKeyValues(context);

        assertThat(keyValues).isEmpty();
    }
}