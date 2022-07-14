package io.jaconi.morp;

import io.jaconi.morp.tenant.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.util.List;
import java.util.Map;

import static io.jaconi.morp.MorpReactiveUserService.ROLE_PROXY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

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
                    assertThat(defaultOidcUser.getAuthorities(), containsInAnyOrder(new SimpleGrantedAuthority(ROLE_PROXY)));
                })
                .verifyComplete();

    }

    @Test
    void testClaimsMatch() {
        doReturn(Map.of("sub", List.of("match"))).when(tenantService).getClaimConstraints(anyString());

        OidcUserRequest oidcUserRequest = getOidcUserRequest();

        Mono<OidcUser> oidcUserMono = morpReactiveUserService.loadUser(oidcUserRequest);
        StepVerifier.create(oidcUserMono)
                .assertNext(o ->
                {
                    DefaultOidcUser defaultOidcUser = (DefaultOidcUser) o;
                    assertThat(defaultOidcUser.getAuthorities(), containsInAnyOrder(new SimpleGrantedAuthority(ROLE_PROXY)));
                })
                .verifyComplete();

    }

    @Test
    void testClaimsDontMatch() {
        doReturn(Map.of("sub", List.of("nomatch"))).when(tenantService).getClaimConstraints(anyString());

        OidcUserRequest oidcUserRequest = getOidcUserRequest();

        Mono<OidcUser> oidcUserMono = morpReactiveUserService.loadUser(oidcUserRequest);
        StepVerifier.create(oidcUserMono)
                .assertNext(o ->
                {
                    DefaultOidcUser defaultOidcUser = (DefaultOidcUser) o;
                    assertThat(defaultOidcUser.getAuthorities(), empty());
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
