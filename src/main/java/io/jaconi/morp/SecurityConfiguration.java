package io.jaconi.morp;

import io.jaconi.morp.filters.TenantExtractionFilter;
import org.springframework.beans.factory.annotation.Autowired;
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

import static io.jaconi.morp.MorpReactiveUserService.ROLE_PROXY;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerAuthenticationEntryPoint serverAuthenticationEntryPoint, ServerHttpSecurity httpSecurity,
                                               FilteringWebHandler webHandler, RouteLocator routeLocator,
                                               GlobalCorsProperties globalCorsProperties, Environment environment) {
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
                .addFilterAfter(new TenantExtractionFilter(webHandler, routeLocator, globalCorsProperties, environment), SecurityWebFiltersOrder.REACTOR_CONTEXT)
                .build();
    }
}
