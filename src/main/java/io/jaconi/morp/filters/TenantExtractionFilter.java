package io.jaconi.morp.filters;

import lombok.NonNull;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * The tenant extraction filter determines a tenant from route predicates. Supported predicates are:
 * <ul>
 *     <li>Host</li>
 *     <li>Path</li>
 *     <li>{@link io.jaconi.morp.predicates.TenantFromHostRoutePredicateFactory TenantFromHost}</li>
 *     <li>{@link io.jaconi.morp.predicates.TenantFromHeaderRoutePredicateFactory TenantFromHeader}</li>
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
@Component
public class TenantExtractionFilter extends RoutePredicateHandlerMapping implements WebFilter {
    public TenantExtractionFilter(FilteringWebHandler webHandler, RouteLocator routeLocator,
                                  GlobalCorsProperties globalCorsProperties, Environment environment) {
        super(webHandler, routeLocator, globalCorsProperties, environment);
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
        // Apply all predicates to determine the tenant. We are aware that this code is executed twice (once here and
        // again by Spring Cloud Gateway). However, changing the order of Spring Cloud Gateway and Spring Security
        // causes side effects (for example, breaking injection of principals inside controllers) and might create
        // security issues as well.
        return lookupRoute(exchange)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .then(chain.filter(exchange));
    }
}
