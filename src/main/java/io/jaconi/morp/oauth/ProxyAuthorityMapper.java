package io.jaconi.morp.oauth;

import io.jaconi.morp.tenant.ClaimConstraintsMatcher;
import io.jaconi.morp.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProxyAuthorityMapper {

    public static final String ROLE_PROXY = "ROLE_PROXY";

    final TenantService tenantService;

    final ClaimConstraintsMatcher claimConstraintsMatcher;

    public Set<GrantedAuthority> mapAuthorities(String tenant, Map<String, Object> claims ) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

        if (claimsMatch(claims, tenant)) {
            mappedAuthorities.add(new SimpleGrantedAuthority(ROLE_PROXY));
        }
        return mappedAuthorities;
    }

    private boolean claimsMatch(Map<String, Object> claims, String tenant) {
        var claimConstraints = tenantService.getClaimConstraints(tenant);
        if (claimConstraints.isEmpty()) {
            return true;
        }
        return claimConstraintsMatcher.matches(claims, claimConstraints);
    }

}
