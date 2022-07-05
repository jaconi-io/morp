package io.jaconi.morp.tenant;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class DynamicServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    private final Function<ServerWebExchange, String> tenantExtractor;

    public DynamicServerAuthenticationEntryPoint(Function<ServerWebExchange, String> tenantExtractor) {
        this.tenantExtractor = tenantExtractor;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return Mono.fromSupplier(() -> tenantExtractor.apply(exchange))
                .switchIfEmpty(Mono.error(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "no tenant")))
                .map(tenant -> new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/%s".formatted(tenant)))
                .flatMap(entryPoint -> entryPoint.commence(exchange, ex));
    }
}
