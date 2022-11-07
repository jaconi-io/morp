package io.jaconi.morp.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class MorpReactiveOidcUserService implements ReactiveOAuth2UserService<OidcUserRequest, OidcUser> {

    final OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();

    final ProxyAuthorityMapper proxyAuthorityMapper;

    @Override
    public Mono<OidcUser> loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return delegate.loadUser(userRequest)
                .map(oidcUser -> {
                    String tenant = userRequest.getClientRegistration().getRegistrationId();
                    return new DefaultOidcUser(proxyAuthorityMapper.mapAuthorities(tenant, oidcUser.getClaims()), oidcUser.getIdToken(),
                            oidcUser.getUserInfo());
                });
    }
}
