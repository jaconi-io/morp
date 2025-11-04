package io.jaconi.morp.filters;

import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;

/**
 * The tenant extraction filter determines a tenant from route predicates. Supported predicates are:
 * <ul>
 *     <li>Host</li>
 *     <li>Path</li>
 *     <li>{@link io.jaconi.morp.predicates.GatewayPredicates#tenantFromHost(String...)} TenantFromHost}</li>
 *     <li>{@link io.jaconi.morp.predicates.GatewayPredicates#tenantFromHeader(String)} TenantFromHeader}</li>
 * </ul>
 * <p>
 * Host and Port predicates are expected to have a URI variable "{tenant}". See README.md for additional details on
 * their configuration.
 * <p>
 * The tenant extraction filter has to be applied <i>before</i> the OAuth filters, otherwise the tenant is unavailable
 * for redirects performed in the {@link io.jaconi.morp.tenant.DynamicServerAuthenticationEntryPoint}.
 * <p>
 * The filters position in the Spring Security filter chain is configured in the
 * {@link io.jaconi.morp.SecurityConfiguration}.
 */
public class TenantExtractionFilter extends OncePerRequestFilter {

	private final RequestMatcher requestMatcher;

	private final RouterFunctionMapping routerFunctionMapping;

	public TenantExtractionFilter(RequestMatcher requestMatcher, RouterFunctionMapping routerFunctionMapping) {
		Assert.notNull(requestMatcher, "requestMatcher cannot be null");
		this.requestMatcher = requestMatcher;
		this.routerFunctionMapping = routerFunctionMapping;
	}

	@SneakyThrows
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
		if (!requestMatcher.matches(request)) {
			filterChain.doFilter(request, response);
			return;
		}
		HandlerExecutionChain handler = routerFunctionMapping.getHandler(request);
		if (handler == null) {
			response.setStatus(404);
			return;
		}

		filterChain.doFilter(request, response);
	}
}
