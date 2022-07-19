package io.jaconi.morp;

import org.reflections.Reflections;
import org.springframework.aot.context.bootstrap.generator.infrastructure.nativex.NativeConfigurationRegistry;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.RouteRefreshListener;
import org.springframework.cloud.gateway.support.Configurable;
import org.springframework.nativex.AotOptions;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

import java.util.Set;

import static org.reflections.scanners.Scanners.SubTypes;

@NativeHint(trigger = GatewayAutoConfiguration.class, types =
        {
                @TypeHint(types = PredicateDefinition.class, access = TypeAccess.PUBLIC_CONSTRUCTORS),
                @TypeHint(types = PredicateDefinition.class, access = TypeAccess.PUBLIC_CONSTRUCTORS),
                @TypeHint(types = RouteRefreshListener.class, access = TypeAccess.PUBLIC_CONSTRUCTORS),
                @TypeHint(types = CachingRouteLocator.class, access = TypeAccess.PUBLIC_CONSTRUCTORS)
        }
)
public class GatewayHints implements NativeConfiguration {

    @Override
    public void computeHints(NativeConfigurationRegistry registry, AotOptions aotOptions) {
        NativeConfigurationRegistry.ReflectionConfiguration reflection = registry.reflection();
        Reflections reflections = new Reflections("org.springframework.cloud.gateway");
        Set<Class<?>> subTypes =
                reflections.get(SubTypes.of(Configurable.class).asClass());
        for (var subType : subTypes) {
            reflection.forType(subType).withAccess(TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS);
            for (var innerClass : subType.getDeclaredClasses()) {
                reflection.forType(innerClass).withAccess(TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS);
            }
        }
    }

}