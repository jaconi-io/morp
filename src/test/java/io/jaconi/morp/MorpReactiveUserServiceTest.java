package io.jaconi.morp;

import io.jaconi.morp.tenant.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.jaconi.morp.MorpReactiveUserService.ROLE_PROXY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@SpringBootTest
class MorpReactiveUserServiceTest {

    @Autowired
    MorpReactiveUserService morpReactiveUserService;

    @MockBean
    TenantService tenantService;

    @Test
    void testNoClaimsConfigured() {
        OidcUserRequest oidcUserRequest = getOidcUserRequest();

        Mono<OidcUser> oidcUserMono = morpReactiveUserService.loadUser(oidcUserRequest);
        StepVerifier.create(oidcUserMono)
                .assertNext(o ->
                {
                    DefaultOidcUser defaultOidcUser = (DefaultOidcUser) o;
                    assertThat((Collection<GrantedAuthority>)defaultOidcUser.getAuthorities()).containsOnly(new SimpleGrantedAuthority(ROLE_PROXY));
                })
                .verifyComplete();

    }

    @Test
    void testClaimsMatch() {
        when(tenantService.getClaimConstraints(anyString())).thenReturn(Map.of("sub", List.of("match")));

        OidcUserRequest oidcUserRequest = getOidcUserRequest();

        Mono<OidcUser> oidcUserMono = morpReactiveUserService.loadUser(oidcUserRequest);
        StepVerifier.create(oidcUserMono)
                .assertNext(o ->
                {
                    DefaultOidcUser defaultOidcUser = (DefaultOidcUser) o;
                    assertThat((Collection<GrantedAuthority>)defaultOidcUser.getAuthorities()).containsOnly(new SimpleGrantedAuthority(ROLE_PROXY));
                })
                .verifyComplete();

    }

    @Test
    void testClaimsDontMatch() {
        when(tenantService.getClaimConstraints(anyString())).thenReturn(Map.of("sub", List.of("nomatch")));

        OidcUserRequest oidcUserRequest = getOidcUserRequest();

        Mono<OidcUser> oidcUserMono = morpReactiveUserService.loadUser(oidcUserRequest);
        StepVerifier.create(oidcUserMono)
                .assertNext(o ->
                {
                    DefaultOidcUser defaultOidcUser = (DefaultOidcUser) o;
                    assertThat(defaultOidcUser.getAuthorities()).isEmpty();
                })
                .verifyComplete();
    }

    private OidcUserRequest getOidcUserRequest() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("tenant1")
                .clientId("id")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("redirect")
                .authorizationUri("auth")
                .tokenUri("token")
                .build();

        OidcIdToken idToken = new OidcIdToken("token", Instant.now(), Instant.MAX, Map.of("sub", "match"));
        return new OidcUserRequest(clientRegistration, mock(OAuth2AccessToken.class), idToken);
    }

}
