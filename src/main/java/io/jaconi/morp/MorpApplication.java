package io.jaconi.morp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
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
        @TypeHint(types = Collections.class, typeNames = "java.util.Collections$UnmodifiableMap", access = TypeAccess.PUBLIC_METHODS),
        @TypeHint(types = SimpleGrantedAuthority.class, access = TypeAccess.PUBLIC_METHODS),
        @TypeHint(typeNames = "org.springframework.core.annotation.TypeMappedAnnotation[]")
})
@EnableCaching
public class MorpApplication {
    public static void main(String[] args) {
        SpringApplication.run(MorpApplication.class, args);
    }

}
