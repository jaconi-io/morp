package io.jaconi.morp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ConfigurationPropertiesScan
@ImportRuntimeHints(MorpRuntimeHints.class)
@EnableCaching
public class MorpApplication {
    public static void main(String[] args) {
        SpringApplication.run(MorpApplication.class, args);
    }

}
