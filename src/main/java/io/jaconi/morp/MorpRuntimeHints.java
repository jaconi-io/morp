package io.jaconi.morp;

import io.jaconi.morp.predicates.TenantFromHeaderRoutePredicateFactory;
import io.jaconi.morp.predicates.TenantFromHostRoutePredicateFactory;
import org.reflections.Reflections;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.RouteRefreshListener;
import org.springframework.cloud.gateway.support.Configurable;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.ClassUtils;

import java.util.Set;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS;

public class MorpRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        // Generic hints
        hints.reflection().registerType(ClassUtils.resolveClassName("com.sun.management.OperatingSystemMXBean", classLoader), INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(JwtDecoder.class, INVOKE_PUBLIC_CONSTRUCTORS);

        // Static Gateway hints
        hints.reflection().registerType(PredicateDefinition.class, INVOKE_PUBLIC_CONSTRUCTORS);
        hints.reflection().registerType(FilterDefinition.class, INVOKE_PUBLIC_CONSTRUCTORS);
        hints.reflection().registerType(RouteRefreshListener.class, INVOKE_PUBLIC_CONSTRUCTORS);
        hints.reflection().registerType(CachingRouteLocator.class, INVOKE_PUBLIC_CONSTRUCTORS);
        hints.reflection().registerType(TenantFromHeaderRoutePredicateFactory.class, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS);
        hints.reflection().registerType(TenantFromHeaderRoutePredicateFactory.Config.class, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS);
        hints.reflection().registerType(TenantFromHostRoutePredicateFactory.class, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS);
        hints.reflection().registerType(TenantFromHostRoutePredicateFactory.Config.class, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS);

        // Dynamic Gateway hints
        Reflections reflections = new Reflections("org.springframework.cloud.gateway");
        Set<Class<?>> subTypes = reflections.get(SubTypes.of(Configurable.class).asClass());
        for (var subType : subTypes) {
            hints.reflection().registerType(subType, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS);
            for (var innerClass : subType.getDeclaredClasses()) {
                hints.reflection().registerType(innerClass, INVOKE_PUBLIC_METHODS, INVOKE_PUBLIC_CONSTRUCTORS);
            }
        }
    }

}