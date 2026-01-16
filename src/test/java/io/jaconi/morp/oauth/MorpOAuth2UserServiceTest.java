package io.jaconi.morp.oauth;

import static io.jaconi.morp.oauth.ProxyAuthorityMapper.ROLE_PROXY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import io.jaconi.morp.tenant.TenantService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MockServerTest
class MorpOAuth2UserServiceTest {

	@Autowired
	MorpOAuth2UserService userService;

	@MockitoBean
	TenantService tenantService;

	MockServerClient mockServerClient;

	@BeforeEach
	void setUp() {
		mockServerClient.reset();
	}

	@Test
	@SuppressWarnings("unchecked")
	void testNoClaimsConfigured() {
		var userRequest = getUserRequest();
		var user = userService.loadUser(userRequest);
		assertThat((Collection<GrantedAuthority>) user.getAuthorities())
				.containsOnly(new SimpleGrantedAuthority(ROLE_PROXY));
	}

	@Test
	@SuppressWarnings("unchecked")
	void testClaimsMatch() {
		when(tenantService.getClaimConstraints(anyString())).thenReturn(Map.of("sub", List.of("match")));

		var userRequest = getUserRequest();
		var user = userService.loadUser(userRequest);
		assertThat((Collection<GrantedAuthority>) user.getAuthorities())
				.containsOnly(new SimpleGrantedAuthority(ROLE_PROXY));
	}

	@Test
	void testClaimsDontMatch() {
		when(tenantService.getClaimConstraints(anyString())).thenReturn(Map.of("sub", List.of("no-match")));

		var userRequest = getUserRequest();
		var user = userService.loadUser(userRequest);
		assertThat(user.getAuthorities()).isEmpty();
	}

	private OAuth2UserRequest getUserRequest() {
		mockServerClient
				.when(request()
						.withMethod("GET")
						.withPath("/userinfo"))
				.respond(response()
						.withBody("""
							{
							  "sub": "match",
							  "awesome_name": "foo"
							}
							""")
						.withContentType(MediaType.APPLICATION_JSON)
						.withStatusCode(200));

		var clientRegistration = ClientRegistration.withRegistrationId("tenant1")
				.clientId("id")
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.redirectUri("redirect")
				.authorizationUri("auth")
				.tokenUri("token")
				.userInfoUri("http://localhost:%s/userinfo".formatted(mockServerClient.getPort()))
				.userNameAttributeName("awesome_name")
				.build();

		return new OAuth2UserRequest(clientRegistration, mock(OAuth2AccessToken.class), Map.of("sub", "match"));
	}
}
