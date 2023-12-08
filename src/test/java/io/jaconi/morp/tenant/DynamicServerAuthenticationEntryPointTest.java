package io.jaconi.morp.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.function.ServerRequest;

class DynamicServerAuthenticationEntryPointTest {

	@Test
	void commence() {
		var entryPoint = new DynamicServerAuthenticationEntryPoint();
		var request = new MockHttpServletRequest("GET", "/foo");
		var response = new MockHttpServletResponse();
		MvcUtils.putUriTemplateVariables(ServerRequest.create(request, List.of()), Map.of("tenant", "foo"));

		entryPoint.commence(request, response, null);

		assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
		assertThat(response.getHeaders("Location").getFirst()).endsWith("/oauth2/authorization/foo");
	}

	@Test
	void commenceNoTenant() {
		var entryPoint = new DynamicServerAuthenticationEntryPoint();
		var request = new MockHttpServletRequest("GET", "/foo");
		var response = new MockHttpServletResponse();
		MvcUtils.putUriTemplateVariables(ServerRequest.create(request, List.of()), Map.of());

		entryPoint.commence(request, response, null);

		assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
	}
}
