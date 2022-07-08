package io.jaconi.morp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.ui.LogoutPageGeneratingWebFilter;

import static io.jaconi.morp.MorpReactiveUserService.ROLE_PROXY;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerAuthenticationEntryPoint serverAuthenticationEntryPoint, ServerHttpSecurity httpSecurity) {
        return httpSecurity.authorizeExchange()
                .pathMatchers("/debug").authenticated()
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
}
