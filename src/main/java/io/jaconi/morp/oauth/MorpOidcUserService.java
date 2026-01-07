package io.jaconi.morp.oauth;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class MorpOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    final OidcUserService delegate = new OidcUserService();

    final ProxyAuthorityMapper proxyAuthorityMapper;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        var delegateUser = delegate.loadUser(userRequest);
        var tenant = userRequest.getClientRegistration().getRegistrationId();
        return new DefaultOidcUser(proxyAuthorityMapper.mapAuthorities(tenant, delegateUser.getClaims()), delegateUser.getIdToken(),
                delegateUser.getUserInfo());
    }
}
