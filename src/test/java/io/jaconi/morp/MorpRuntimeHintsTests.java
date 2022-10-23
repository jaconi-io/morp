package io.jaconi.morp;

import com.sun.management.OperatingSystemMXBean;
import io.jaconi.morp.predicates.TenantFromHeaderRoutePredicateFactory;
import io.jaconi.morp.predicates.TenantFromHostRoutePredicateFactory;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.RouteRefreshListener;
import org.springframework.cloud.gateway.support.Configurable;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.reflections.scanners.Scanners.SubTypes;
import static org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;

class MorpRuntimeHintsTests {

    @Test
    void shouldRegisterHints() {
        RuntimeHints hints = new RuntimeHints();
        new MorpRuntimeHints().registerHints(hints, getClass().getClassLoader());

        // Generic hints
        assertThat(reflection().onType(JwtDecoder.class).withMemberCategory(INVOKE_PUBLIC_CONSTRUCTORS)).accepts(hints);
        assertThat(reflection().onType(OperatingSystemMXBean.class)).accepts(hints);

        // Static Gateway hints
        assertThat(reflection().onType(PredicateDefinition.class).withMemberCategory(INVOKE_PUBLIC_CONSTRUCTORS)).accepts(hints);
        assertThat(reflection().onType(FilterDefinition.class).withMemberCategory(INVOKE_PUBLIC_CONSTRUCTORS)).accepts(hints);
        assertThat(reflection().onType(RouteRefreshListener.class).withMemberCategory(INVOKE_PUBLIC_CONSTRUCTORS)).accepts(hints);
        assertThat(reflection().onType(CachingRouteLocator.class).withMemberCategory(INVOKE_PUBLIC_CONSTRUCTORS)).accepts(hints);
        assertThat(reflection().onType(TenantFromHeaderRoutePredicateFactory.class).withMemberCategories(INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)).accepts(hints);
        assertThat(reflection().onType(TenantFromHeaderRoutePredicateFactory.Config.class).withMemberCategories(INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)).accepts(hints);
        assertThat(reflection().onType(TenantFromHostRoutePredicateFactory.class).withMemberCategories(INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)).accepts(hints);
        assertThat(reflection().onType(TenantFromHostRoutePredicateFactory.Config.class).withMemberCategories(INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)).accepts(hints);

        // Dynamic Gateway hints
        Reflections reflections = new Reflections("org.springframework.cloud.gateway");
        Set<Class<?>> subTypes = reflections.get(SubTypes.of(Configurable.class).asClass());
        for (var subType : subTypes) {
            // Skip classes when TypeReference doesn't work for whatever reason
            if (TypeReference.of(subType).getCanonicalName() == null) {
                continue;
            }
            assertThat(reflection().onType(subType).withMemberCategories(INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)).accepts(hints);
            for (var innerClass : subType.getDeclaredClasses()) {
                assertThat(reflection().onType(innerClass).withMemberCategories(INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS)).accepts(hints);
            }
        }

    }

}