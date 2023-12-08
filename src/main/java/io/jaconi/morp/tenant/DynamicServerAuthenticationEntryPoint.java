package io.jaconi.morp.tenant;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

@Component
public class DynamicServerAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) {
		TenantExtractor.extractTenant(ServerRequest.create(request, List.of()))
				.ifPresentOrElse(t -> sendRedirect(response, t),
						() -> sendError(response)
				);

	}

	@SneakyThrows(IOException.class)
	private static void sendError(HttpServletResponse response) {
		response.sendError(500, "No tenant.");
	}

	@SneakyThrows(IOException.class)
	private void sendRedirect(HttpServletResponse response, String tenant) {
		response.sendRedirect("/oauth2/authorization/%s".formatted(tenant));
	}
}
