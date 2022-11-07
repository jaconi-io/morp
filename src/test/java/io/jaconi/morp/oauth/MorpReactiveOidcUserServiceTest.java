package io.jaconi.morp.oauth;

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
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.jaconi.morp.oauth.ProxyAuthorityMapper.ROLE_PROXY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class MorpReactiveOidcUserServiceTest {

    @Autowired
    MorpReactiveOidcUserService userService;

    @MockBean
    TenantService tenantService;

    @Test
    @SuppressWarnings("unchecked")
    void testNoClaimsConfigured() {
        var userRequest = getUserRequest();
        var user = userService.loadUser(userRequest);
        StepVerifier.create(user)
                .assertNext(o ->
                {
                    var defaultUser = (DefaultOidcUser) o;
                    assertThat((Collection<GrantedAuthority>) defaultUser.getAuthorities()).containsOnly(new SimpleGrantedAuthority(ROLE_PROXY));
                })
                .verifyComplete();

    }

    @Test
    @SuppressWarnings("unchecked")
    void testClaimsMatch() {
        when(tenantService.getClaimConstraints(anyString())).thenReturn(Map.of("sub", List.of("match")));

        var userRequest = getUserRequest();
        var user = userService.loadUser(userRequest);
        StepVerifier.create(user)
                .assertNext(o ->
                {
                    var defaultUser = (DefaultOidcUser) o;
                    assertThat((Collection<GrantedAuthority>) defaultUser.getAuthorities()).containsOnly(new SimpleGrantedAuthority(ROLE_PROXY));
                })
                .verifyComplete();

    }

    @Test
    void testClaimsDontMatch() {
        when(tenantService.getClaimConstraints(anyString())).thenReturn(Map.of("sub", List.of("nomatch")));

        var userRequest = getUserRequest();
        var user = userService.loadUser(userRequest);
        StepVerifier.create(user)
                .assertNext(o ->
                {
                    var defaultUser = (DefaultOidcUser) o;
                    assertThat(defaultUser.getAuthorities()).isEmpty();
                })
                .verifyComplete();
    }

    private OidcUserRequest getUserRequest() {
        var clientRegistration = ClientRegistration.withRegistrationId("tenant1")
                .clientId("id")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("redirect")
                .authorizationUri("auth")
                .tokenUri("token")
                .build();

        var idToken = new OidcIdToken("token", Instant.now(), Instant.MAX, Map.of("sub", "match"));
        return new OidcUserRequest(clientRegistration, mock(OAuth2AccessToken.class), idToken);
    }
}
