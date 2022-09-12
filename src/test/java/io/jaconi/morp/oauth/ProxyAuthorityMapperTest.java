package io.jaconi.morp.oauth;

import io.jaconi.morp.tenant.ClaimConstraintsMatcher;
import io.jaconi.morp.tenant.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ProxyAuthorityMapperTest {

    @Mock
    ClaimConstraintsMatcher claimConstraintsMatcher;

    @Mock
    TenantService tenantService;

    @InjectMocks
    ProxyAuthorityMapper proxyAuthorityMapper;

    @BeforeEach
    void init_mocks() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    void noClaimConstraints() {
        when(tenantService.getClaimConstraints(anyString())).thenReturn(Collections.emptyMap());
        when(claimConstraintsMatcher.matches(anyMap(), anyMap())).thenReturn(false);
        assertThat(proxyAuthorityMapper.mapAuthorities("tenant", Collections.emptyMap()))
                .containsExactly(new SimpleGrantedAuthority(ProxyAuthorityMapper.ROLE_PROXY));
    }

    @Test
    void claimConstraintsMatch() {
        when(tenantService.getClaimConstraints(anyString())).thenReturn(Map.of("a", List.of("b")));
        when(claimConstraintsMatcher.matches(anyMap(), anyMap())).thenReturn(true);
        assertThat(proxyAuthorityMapper.mapAuthorities("tenant", Collections.emptyMap()))
                .containsExactly(new SimpleGrantedAuthority(ProxyAuthorityMapper.ROLE_PROXY));
    }

    @Test
    void claimConstraintsDontMatch() {
        when(tenantService.getClaimConstraints(anyString())).thenReturn(Map.of("a", List.of("b")));
        when(claimConstraintsMatcher.matches(anyMap(), anyMap())).thenReturn(false);
        assertThat(proxyAuthorityMapper.mapAuthorities("tenant", Collections.emptyMap()))
                .isEmpty();
    }


}