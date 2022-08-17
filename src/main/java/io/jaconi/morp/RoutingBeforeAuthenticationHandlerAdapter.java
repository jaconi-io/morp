package io.jaconi.morp;

import lombok.NonNull;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.handler.FilteringWebHandler;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom {@link HandlerAdapter}, applicable to the Spring Cloud Gateway handler. It ensures the Spring Security
 * filter chain is applied, before any requests are handled. However, this allows the route predicates to be
 * evaluated before the security filter chain is applied. We need this, to get access to the extracted tenant URI
 * variables in the {@link io.jaconi.morp.tenant.DynamicServerAuthenticationEntryPoint}.
 */
@Component
public class RoutingBeforeAuthenticationHandlerAdapter implements HandlerAdapter {
    private final List<WebFilter> securityFilterChain;

    public RoutingBeforeAuthenticationHandlerAdapter() {
        securityFilterChain = new ArrayList<>(1);
    }

    public void setSecurityFilterChain(WebFilterChainProxy webFilterChainProxy) {
        securityFilterChain.clear();
        securityFilterChain.add(webFilterChainProxy);
    }

    @Override
    public boolean supports(@NonNull Object handler) {
        return handler instanceof org.springframework.cloud.gateway.handler.FilteringWebHandler;
    }

    @Override
    public @NonNull Mono<HandlerResult> handle(@NonNull ServerWebExchange exchange, @NonNull Object handler) {
        var filteringWebHandler = new FilteringWebHandler((WebHandler) handler, securityFilterChain);
        return filteringWebHandler.handle(exchange).then(Mono.empty());
    }
}
