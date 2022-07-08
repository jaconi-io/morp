package io.jaconi.morp;

import io.jaconi.morp.tenant.ClaimConstraintsMatcher;
import io.jaconi.morp.tenant.TenantProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.ui.LogoutPageGeneratingWebFilter;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class SecurityConfiguration {

    public static final String ROLE_PROXY = "ROLE_PROXY";

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

    @Bean
    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(ClaimConstraintsMatcher claimConstraintsMatcher, TenantProperties tenantProperties) {
        final OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();

        return (userRequest) -> {
            // Delegate to the default implementation for loading a user
            return delegate.loadUser(userRequest)
                    .map((oidcUser) -> {
                        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

                        String tenant = userRequest.getClientRegistration().getRegistrationId();

                        if (!tenantProperties.tenant().containsKey(tenant) ||
                                tenantProperties.tenant().get(tenant).claimConstraints() == null
                                || tenantProperties.tenant().get(tenant).claimConstraints().isEmpty()
                                || claimConstraintsMatcher.matches(oidcUser.getClaims(), tenantProperties.tenant().get(tenant).claimConstraints())) {
                            mappedAuthorities.add(new SimpleGrantedAuthority(ROLE_PROXY));
                        }

                        return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
                    });
        };
    }
}
