package io.jaconi.morp;

import io.jaconi.morp.tenant.ClaimConstraintsMatcher;
import io.jaconi.morp.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class MorpReactiveUserService implements ReactiveOAuth2UserService<OidcUserRequest, OidcUser> {

    public static final String ROLE_PROXY = "ROLE_PROXY";

    final OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();

    final TenantService tenantService;

    final ClaimConstraintsMatcher claimConstraintsMatcher;

    @Override
    public Mono<OidcUser> loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return delegate.loadUser(userRequest)
                .map((oidcUser) -> {
                    Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

                    String tenant = userRequest.getClientRegistration().getRegistrationId();

                    if (claimsMatch(oidcUser, tenant)) {
                        mappedAuthorities.add(new SimpleGrantedAuthority(ROLE_PROXY));
                    }

                    return new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
                });
    }

    boolean claimsMatch(OidcUser oidcUser, String tenant) {
        var claimConstraints = tenantService.getClaimConstraints(tenant);
        if (claimConstraints.isEmpty()) {
            return true;
        }
        return claimConstraintsMatcher.matches(oidcUser.getClaims(), claimConstraints);
    }
}
