package io.jaconi.morp.oauth;

import com.google.common.collect.Maps;
import io.jaconi.morp.tenant.TenantProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Oauth2ClientDiscovererTest {

    private Oauth2ClientDiscoverer discoverer(String tenant, String tenantClient, String defaultClient) {
        return new Oauth2ClientDiscoverer(new TenantProperties(Collections.singletonMap("foo",
                new TenantProperties.TenantSettings(null, null, tenantClient)), defaultClient));
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    void test(Map<String, String> tenantSpecificClients, String defaultClient, String requestedTenant, String expectedClient) {

        Map<String, TenantProperties.TenantSettings> tenantMap = tenantSpecificClients
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new TenantProperties.TenantSettings(null, null, e.getValue())));

        Oauth2ClientDiscoverer discoverer = new Oauth2ClientDiscoverer(new TenantProperties(tenantMap, defaultClient));

        assertEquals(expectedClient, discoverer.discoverOauth2Client(requestedTenant));
    }

    private static Stream<Arguments> provideArguments() {
        return Stream.of(
                Arguments.of(Map.of(), "google", "foo", "google"),
                Arguments.of(Map.of("foo", "keycloak"), "google", "foo", "keycloak"),
                Arguments.of(Map.of("foo", "keycloak", "bar", "okta"), "google", "foobar", "google"),
                Arguments.of(Map.of("foo", "keycloak", "bar", "okta"), "google", "bar", "okta")
        );
    }

}
