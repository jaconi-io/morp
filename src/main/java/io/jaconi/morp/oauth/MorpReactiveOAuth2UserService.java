package io.jaconi.morp.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class MorpReactiveOAuth2UserService implements ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {

    final DefaultReactiveOAuth2UserService delegate = new DefaultReactiveOAuth2UserService();

    final ProxyAuthorityMapper proxyAuthorityMapper;

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        return delegate.loadUser(userRequest)
                .map(user -> {
                    String tenant = userRequest.getClientRegistration().getRegistrationId();
                    String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
                    return new DefaultOAuth2User(proxyAuthorityMapper.mapAuthorities(tenant, user.getAttributes()), user.getAttributes(),
                            userNameAttributeName);
                });
    }

}
