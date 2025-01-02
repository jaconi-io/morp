package io.jaconi.morp.oauth;

import io.jaconi.morp.tenant.TenantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.test.StepVerifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static io.jaconi.morp.oauth.ProxyAuthorityMapper.ROLE_PROXY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@DisabledInAotMode
class MorpReactiveOAuth2UserServiceTest {

    @Autowired
    MorpReactiveOAuth2UserService userService;

    @MockitoBean
    TenantService tenantService;

    @Test
    @SuppressWarnings("unchecked")
    void testNoClaimsConfigured() {
        var userRequest = getUserRequest();
        var user = userService.loadUser(userRequest);
        StepVerifier.create(user)
                .assertNext(o ->
                {
                    var defaultUser = (DefaultOAuth2User) o;
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
                    var defaultUser = (DefaultOAuth2User) o;
                    assertThat((Collection<GrantedAuthority>) defaultUser.getAuthorities()).containsOnly(new SimpleGrantedAuthority(ROLE_PROXY));
                })
                .verifyComplete();
    }

    @Test
    void testClaimsDontMatch() {
        when(tenantService.getClaimConstraints(anyString())).thenReturn(Map.of("sub", List.of("no-match")));

        var userRequest = getUserRequest();
        var user = userService.loadUser(userRequest);
        StepVerifier.create(user)
                .assertNext(o ->
                {
                    var defaultUser = (DefaultOAuth2User) o;
                    assertThat(defaultUser.getAuthorities()).isEmpty();
                })
                .verifyComplete();
    }

    private OAuth2UserRequest getUserRequest() {
        serveUserInfoEndpointOnce();

        var clientRegistration = ClientRegistration.withRegistrationId("tenant1")
                .clientId("id")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("redirect")
                .authorizationUri("auth")
                .tokenUri("token")
                .userInfoUri("http://localhost:8089/userinfo")
                .userNameAttributeName("awesome_name")
                .build();

        return new OAuth2UserRequest(clientRegistration, mock(OAuth2AccessToken.class), Map.of("sub", "match"));
    }

    private void serveUserInfoEndpointOnce() {
        var executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try (var serverSocket = new ServerSocket(8089)) {
                var clientSocket = serverSocket.accept();
                var out = new PrintWriter(clientSocket.getOutputStream(), true);
                var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                var req = in.readLine();
                if ("GET /userinfo HTTP/1.1".equals(req)) {
                    var resp = """
                            {
                              "sub": "match",
                              "awesome_name": "foo"
                            }
                            """;
                    out.println("HTTP/1.1 200 OK");
                    out.println("Connection: close");
                    out.println("Content-Type: application/json");
                    out.println("Content-Length:" + resp.length());
                    out.println();
                    out.println(resp);
                } else {
                    out.println("HTTP/1.1 404 Not Found");
                    out.println("Connection: close");
                    out.println();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();

        // Wait for server to start.
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
