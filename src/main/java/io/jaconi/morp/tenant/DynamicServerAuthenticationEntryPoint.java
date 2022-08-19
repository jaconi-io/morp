package io.jaconi.morp.tenant;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class DynamicServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        var templateVariables = ServerWebExchangeUtils.getUriTemplateVariables(exchange);
        var tenant = templateVariables.get("tenant");
        if (tenant == null) {
            return Mono.error(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "no tenant"));
        }

        var redirect = new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/%s".formatted(tenant));
        return redirect.commence(exchange, ex);
    }
}
