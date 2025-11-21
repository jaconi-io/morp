package io.jaconi.morp.oauth;

import static io.jaconi.morp.oauth.ProxyAuthorityMapper.ROLE_PROXY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import io.jaconi.morp.tenant.TenantService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MorpOidcUserServiceTest {

	@Autowired
	MorpOidcUserService userService;

	@MockitoBean
	TenantService tenantService;

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
		when(tenantService.getClaimConstraints(anyString())).thenReturn(Map.of("sub", List.of("nomatch")));

		var userRequest = getUserRequest();
		var user = userService.loadUser(userRequest);
		assertThat(user.getAuthorities()).isEmpty();
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
