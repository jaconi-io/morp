package io.jaconi.morp.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.function.ServerRequest;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;

class TenantObservationConventionTest {

    @Test
    void fooTenant() {
        var request = new MockHttpServletRequest("GET", "/foo");
        var response = new MockHttpServletResponse();
        var context = new ServerRequestObservationContext(request, response);

        MvcUtils.putUriTemplateVariables(ServerRequest.create(request, List.of()), Map.of("tenant", "foo"));

        TenantObservationConvention tenantObservationConvention = new TenantObservationConvention();

        KeyValues keyValues = tenantObservationConvention.getLowCardinalityKeyValues(context);

        assertThat(keyValues).contains(KeyValue.of("tenant", "foo"));
    }

    @Test
    void noTenant() {
        var request = new MockHttpServletRequest("GET", "/foo");
        var response = new MockHttpServletResponse();
        var context = new ServerRequestObservationContext(request, response);

        MvcUtils.putUriTemplateVariables(ServerRequest.create(request, List.of()), Map.of());

        TenantObservationConvention tenantObservationConvention = new TenantObservationConvention();

        KeyValues keyValues = tenantObservationConvention.getLowCardinalityKeyValues(context);

        assertThat(keyValues).noneMatch(p -> p.getKey().equals("tenant"));
    }
}