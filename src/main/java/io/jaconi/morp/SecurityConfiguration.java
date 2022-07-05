package io.jaconi.morp;

import io.jaconi.morp.idp.IDPMapper;
import io.jaconi.morp.oauth.JaconiOAuth2ClientProperties;
import io.jaconi.morp.oauth.TenantAwareClientRegistrationRepository;
import io.jaconi.morp.tenant.TenantCredentials;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.ui.LogoutPageGeneratingWebFilter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerAuthenticationEntryPoint serverAuthenticationEntryPoint, ServerHttpSecurity httpSecurity) {
        return httpSecurity.authorizeExchange()
                .anyExchange().authenticated()
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
    TenantAwareClientRegistrationRepository clientRegistrationRepository(JaconiOAuth2ClientProperties properties, IDPMapper idpMapper, TenantCredentials tenantCredentials) {
        List<ClientRegistration> registrations = new ArrayList<>(
                OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(properties).values());

        return new TenantAwareClientRegistrationRepository(registrations, idpMapper, tenantCredentials);
    }
}
