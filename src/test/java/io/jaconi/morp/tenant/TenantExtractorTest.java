package io.jaconi.morp.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.ServerRequest;

class TenantExtractorTest {

	@Test
	void extractFooTenant() {
		var servletRequest = new MockHttpServletRequest("GET", "/foo");
		var request = ServerRequest.create(servletRequest, List.of());

		MvcUtils.putUriTemplateVariables(request, Map.of("tenant", "foo"));

		Optional<String> tenant = TenantExtractor.extractTenant(request);

		assertThat(tenant).contains("foo");
	}

	@Test
	void extractNoTenant() {
		var servletRequest = new MockHttpServletRequest("GET", "/foo");
		var request = ServerRequest.create(servletRequest, List.of());

		MvcUtils.putUriTemplateVariables(request, Map.of());

		Optional<String> tenant = TenantExtractor.extractTenant(request);

		assertThat(tenant).isEmpty();
	}

}