package io.jaconi.morp.predicates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.ServerRequest;

class TenantFromHostRoutePredicateFactoryTest {

    @Test
    void applyMatch() {
        var predicate = GatewayPredicates.tenantFromHost("foo", "match.example.com");

        var request = new MockHttpServletRequest("GET", "/test");
        request.addHeader("Host", "match.example.com");
        var serverRequest = ServerRequest.create(request, List.of());

        assertThat(predicate.test(serverRequest)).isTrue();
        assertThat(MvcUtils.getUriTemplateVariables(serverRequest)).contains(entry("tenant", "foo"));
    }

    @Test
    void applyNoMatch() {
        var predicate = GatewayPredicates.tenantFromHost("foo", "static.example.com");

        var request = new MockHttpServletRequest("GET", "/test");
        request.addHeader("Host", "mismatch.example.com");
        var serverRequest = ServerRequest.create(request, List.of());

        assertThat(predicate.test(serverRequest)).isFalse();
        assertThat(MvcUtils.getUriTemplateVariables(serverRequest)).doesNotContainKey("tenant");
    }

}
