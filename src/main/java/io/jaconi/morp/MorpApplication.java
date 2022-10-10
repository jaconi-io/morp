package io.jaconi.morp;

import net.logstash.logback.encoder.LogstashEncoder;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.nativex.hint.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;

import java.util.Collections;

@SpringBootApplication
@ConfigurationPropertiesScan
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
    },
    // Remove warning when building the native image
    initialization = {
        @InitializationHint(typeNames = "io.netty.handler.ssl.BouncyCastleAlpnSslUtils", initTime = InitializationTime.RUN)
    }
)
@EnableCaching
public class MorpApplication {
    public static void main(String[] args) {
        SpringApplication.run(MorpApplication.class, args);
    }

}
