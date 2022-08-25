package io.jaconi.morp.filters;

import lombok.NonNull;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

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
public class TenantExtractionFilter extends RoutePredicateHandlerMapping implements WebFilter {

    private final ServerWebExchangeMatcher exchangeMatcher;

    public TenantExtractionFilter(FilteringWebHandler webHandler, RouteLocator routeLocator,
                                  GlobalCorsProperties globalCorsProperties, Environment environment,
                                  ServerWebExchangeMatcher exchangeMatcher) {
        super(webHandler, routeLocator, globalCorsProperties, environment);
        Assert.notNull(exchangeMatcher, "exchangeMatcher cannot be null");
        this.exchangeMatcher = exchangeMatcher;
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
        // Apply all predicates to determine the tenant. We are aware that this code is executed twice (once here and
        // again by Spring Cloud Gateway). However, changing the order of Spring Cloud Gateway and Spring Security
        // causes side effects (for example, breaking injection of principals inside controllers) and might create
        // security issues as well.
        return this.exchangeMatcher.matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .filterWhen(b -> isNotAuthenticated())
                .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
                .flatMap(r -> lookupTenant(exchange, chain));
    }

    private Mono<Void> lookupTenant(ServerWebExchange exchange, WebFilterChain chain) {
        return lookupRoute(exchange)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                        .then(chain.filter(exchange));
    }

    private Mono<Boolean> isNotAuthenticated() {
        return ReactiveSecurityContextHolder.getContext().filter((c) -> c.getAuthentication() != null)
                .map(SecurityContext::getAuthentication)
                .map(Authentication::isAuthenticated)
                .switchIfEmpty(Mono.just(Boolean.FALSE))
                .map(r -> !r);
    }
}
