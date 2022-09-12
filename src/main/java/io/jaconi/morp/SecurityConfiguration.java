package io.jaconi.morp;

import io.jaconi.morp.filters.RemoveSessionCookieFilter;
import io.jaconi.morp.filters.TenantExtractionFilter;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.ui.LogoutPageGeneratingWebFilter;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import static io.jaconi.morp.oauth.ProxyAuthorityMapper.ROLE_PROXY;

@Configuration
public class SecurityConfiguration {

    private static final String DEBUG_ENDPOINT = "/debug";

    private static final NegatedServerWebExchangeMatcher TENANT_EXTRACTION_REQUEST_MATCHER = new NegatedServerWebExchangeMatcher(
            new OrServerWebExchangeMatcher(
                    new PathPatternParserServerWebExchangeMatcher(DEBUG_ENDPOINT),
                    new PathPatternParserServerWebExchangeMatcher("/oauth2/**"),
                    new PathPatternParserServerWebExchangeMatcher("/login/oauth2/**"),
                    EndpointRequest.toAnyEndpoint()
            )
    );

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerAuthenticationEntryPoint serverAuthenticationEntryPoint, ServerHttpSecurity httpSecurity,
                                               FilteringWebHandler webHandler, RouteLocator routeLocator,
                                               GlobalCorsProperties globalCorsProperties, Environment environment) {
        return httpSecurity.authorizeExchange()
                .pathMatchers(DEBUG_ENDPOINT).authenticated()
                .matchers(EndpointRequest.toAnyEndpoint()).permitAll()
                .anyExchange().hasAuthority(ROLE_PROXY)
                .and()
                .oauth2Login()
                .and()
                .oauth2Client()
                .and()
                .exceptionHandling().authenticationEntryPoint(serverAuthenticationEntryPoint)
                .and()
                .logout()
                .and()
                .addFilterAt(new LogoutPageGeneratingWebFilter(),
                        SecurityWebFiltersOrder.LOGOUT_PAGE_GENERATING)
                .addFilterAfter(new TenantExtractionFilter(webHandler, routeLocator, globalCorsProperties, environment,
                        TENANT_EXTRACTION_REQUEST_MATCHER), SecurityWebFiltersOrder.REACTOR_CONTEXT)
                .build();
    }

    @Bean
    public RemoveSessionCookieFilter removeSessionCookie(WebSessionIdResolver webSessionIdResolver) {
        if (!(webSessionIdResolver instanceof CookieWebSessionIdResolver)) {
            return null;
        }
        return new RemoveSessionCookieFilter(((CookieWebSessionIdResolver) webSessionIdResolver).getCookieName());
    }

}
