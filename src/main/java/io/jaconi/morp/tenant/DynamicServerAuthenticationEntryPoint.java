package io.jaconi.morp.tenant;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class DynamicServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    private final TenantExtractors tenantExtractors;

    public DynamicServerAuthenticationEntryPoint(TenantExtractors tenantExtractors) {
        this.tenantExtractors = tenantExtractors;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return Mono.fromSupplier(() -> tenantExtractors.apply(exchange))
                .map(tenant -> new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/%s".formatted(tenant)))
                .flatMap(entryPoint -> entryPoint.commence(exchange, ex));
    }
}
