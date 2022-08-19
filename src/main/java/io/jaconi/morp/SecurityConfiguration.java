package io.jaconi.morp;

import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.security.web.server.ui.LogoutPageGeneratingWebFilter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import static io.jaconi.morp.MorpReactiveUserService.ROLE_PROXY;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerAuthenticationEntryPoint serverAuthenticationEntryPoint, ServerHttpSecurity httpSecurity) {
        return httpSecurity.authorizeExchange()
                .pathMatchers("/debug").authenticated()
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
                .build();
    }

    @Bean
    public RouteLocator requiredRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // These routes are required to prevent Spring Cloud Gateway from applying predicates to Spring Security
                // endpoints. The URIs are irrelevant, as only the predicate will be checked, before the Spring Security
                // filter chain kicks in and performs a redirect anyway.
                .route(p -> p
                        .path("/oauth2/**")
                        .uri("http://localhost"))
                .route(p -> p
                        .path("/login/oauth2/**")
                        .uri("http://localhost")
                )
                .build();
    }

    /**
     * Use a customized {@link HttpHandler}, removing the Spring Security filter chain ({@link WebFilterChainProxy}).
     * The filter chain is set at the {@link RoutingBeforeAuthenticationHandlerAdapter} instead.
     *
     * @see <a href="https://github.com/spring-projects/spring-security/issues/5916#issuecomment-429134784">Relevant GitHub Issue</a>
     */
    @Bean
    public HttpHandler httpHandler(ApplicationContext applicationContext, RoutingBeforeAuthenticationHandlerAdapter handlerAdapter) {
        WebHttpHandlerBuilder webHttpHandlerBuilder = WebHttpHandlerBuilder.applicationContext(applicationContext);
        webHttpHandlerBuilder.filters(filters -> filters.stream()
                .filter(f -> f instanceof WebFilterChainProxy)
                .map(f -> (WebFilterChainProxy) f)
                .findFirst()
                .ifPresent(handlerAdapter::setSecurityFilterChain)
        );

        return webHttpHandlerBuilder.filters(filters -> filters.removeIf(f -> f instanceof WebFilterChainProxy)).build();
    }
}
