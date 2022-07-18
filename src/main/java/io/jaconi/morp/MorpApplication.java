package io.jaconi.morp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.CachingRouteLocator;
import org.springframework.cloud.gateway.route.RouteRefreshListener;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;

import java.util.Collections;

@SpringBootApplication
@ConfigurationPropertiesScan
@NativeHint(types = {
        @TypeHint(types = OptionalValidatorFactoryBean.class),
        @TypeHint(types = PredicateDefinition.class, access = TypeAccess.PUBLIC_CONSTRUCTORS),
        @TypeHint(types = PathRoutePredicateFactory.Config.class, access = TypeAccess.PUBLIC_CONSTRUCTORS),
        @TypeHint(types = ConfigurationService.class, access = TypeAccess.PUBLIC_CONSTRUCTORS),
        @TypeHint(types = RouteRefreshListener.class, access = TypeAccess.PUBLIC_CONSTRUCTORS),
        @TypeHint(types = CachingRouteLocator.class, access = TypeAccess.PUBLIC_CONSTRUCTORS),
        @TypeHint(types = Collections.class, typeNames = "java.util.Collections$UnmodifiableMap", access = TypeAccess.PUBLIC_METHODS),
        @TypeHint(types = SimpleGrantedAuthority.class, access = TypeAccess.PUBLIC_METHODS)
})
public class MorpApplication {
    public static void main(String[] args) {
        SpringApplication.run(MorpApplication.class, args);
    }
}
