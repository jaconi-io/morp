package io.jaconi.morp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@ConfigurationPropertiesScan
/*
@NativeHint(types = {
        @TypeHint(types = OptionalValidatorFactoryBean.class),
        @TypeHint(types = Collections.class, typeNames = "java.util.Collections$UnmodifiableMap", access = TypeAccess.PUBLIC_METHODS),
        @TypeHint(types = SimpleGrantedAuthority.class, access = TypeAccess.PUBLIC_METHODS),
        @TypeHint(typeNames = "org.springframework.core.annotation.TypeMappedAnnotation[]"),
        // Logback
        @TypeHint(
                types = {LogstashEncoder.class, ShortenedThrowableConverter.class},
                access = {TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS}
        ),
})
*/
@EnableCaching
public class MorpApplication {
    public static void main(String[] args) {
        SpringApplication.run(MorpApplication.class, args);
    }

}
