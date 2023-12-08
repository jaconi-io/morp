package io.jaconi.morp.tenant;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class DynamicServerAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) {
		TenantExtractor.extractTenant(ServerRequest.create(request, List.of()))
				.ifPresentOrElse(t -> {
							try {
								response.sendRedirect("/oauth2/authorization/%s".formatted(t));
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						},
						() -> {
							try {
								response.sendError(500, "No tenant.");
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
				);

	}
}
