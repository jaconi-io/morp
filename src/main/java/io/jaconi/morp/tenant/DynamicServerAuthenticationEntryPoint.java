package io.jaconi.morp.tenant;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class DynamicServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    private final ServerWebExchangeMatcher matcher;

    public DynamicServerAuthenticationEntryPoint(ServerWebExchangeMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return matcher.matches(exchange)
                .flatMap(matchResult -> {
                    if (!matchResult.isMatch()) {
                        return Mono.error(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "no tenant"));
                    }

                    var tenant = matchResult.getVariables().get("tenant").toString();
                    var redirect = new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/%s".formatted(tenant));
                    return redirect.commence(exchange, ex);
                });
    }
}
