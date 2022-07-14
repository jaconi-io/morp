package io.jaconi.morp.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Pattern;

@ConfigurationProperties(prefix = "morp.cache")
@Validated
public record CacheProperties(
        @Pattern(regexp = "^(redis|caffeine)$", message = "Invalid cache type (only 'redis' and 'caffeine' are supported).") String type,
        int durationInMinutes) {

}
