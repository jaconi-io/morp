package io.jaconi.morp.tenant;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TenantWebFluxTagsContributerTest {

    @Test
    void fooTenant() {
        var request = MockServerHttpRequest.get("/foo").build();
        var exchange = new MockServerWebExchange.Builder(request).build();
        ServerWebExchangeUtils.putUriTemplateVariables(exchange, Map.of("tenant", "foo"));

        TenantWebFluxTagsContributor tenantWebFluxTagsContributor = new TenantWebFluxTagsContributor();

        Iterable<Tag> tags = tenantWebFluxTagsContributor.httpRequestTags(exchange, null);

        assertThat(tags).containsExactly(Tag.of("tenant", "foo"));
    }

    @Test
    void noTenant() {
        var request = MockServerHttpRequest.get("/foo").build();
        var exchange = new MockServerWebExchange.Builder(request).build();
        ServerWebExchangeUtils.putUriTemplateVariables(exchange, Map.of());

        TenantWebFluxTagsContributor tenantWebFluxTagsContributor = new TenantWebFluxTagsContributor();

        Iterable<Tag> tags = tenantWebFluxTagsContributor.httpRequestTags(exchange, null);

        assertThat(tags).isEmpty();
    }
}