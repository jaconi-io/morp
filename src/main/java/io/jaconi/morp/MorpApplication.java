package io.jaconi.morp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;

@SpringBootApplication
@TypeHint(types = OptionalValidatorFactoryBean.class)
@ConfigurationPropertiesScan
public class MorpApplication {
    public static void main(String[] args) {
        SpringApplication.run(MorpApplication.class, args);
    }
}
