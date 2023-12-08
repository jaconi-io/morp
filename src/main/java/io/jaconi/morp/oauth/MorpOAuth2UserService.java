package io.jaconi.morp.oauth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class MorpOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

	final ProxyAuthorityMapper proxyAuthorityMapper;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		var delegateUser = delegate.loadUser(userRequest);
		var tenant = userRequest.getClientRegistration().getRegistrationId();
		String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
				.getUserNameAttributeName();
		return new DefaultOAuth2User(proxyAuthorityMapper.mapAuthorities(tenant, delegateUser.getAttributes()),
				delegateUser.getAttributes(),
				userNameAttributeName);
	}

}
