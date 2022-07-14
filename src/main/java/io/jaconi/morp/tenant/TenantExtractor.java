package io.jaconi.morp.tenant;

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * A tenant extractor uses a {@link ServerWebExchange} (or, more specifically, a
 * {@link org.springframework.http.server.ServerHttpRequest}) to determine the tenant ID.
 * <p>
 * The tenant extractors are implemented in this enum, to simplify polymorph configuration with Spring.
 */
@SuppressWarnings("unused")
public enum TenantExtractor implements Function<Map<String, String>, ServerWebExchangeMatcher> {

    /**
     * Extract the tenant ID from an HTTP header. The configuration is expected to look like this:
     * <pre>
     * header:
     *   name: X-Tenant-ID
     * </pre>
     */
    HEADER {
        @Override
        public ServerWebExchangeMatcher apply(Map<String, String> config) {
            validate(config, Set.of("name"), Collections.emptySet())
                    .ifPresent(s -> {
                        throw new IllegalArgumentException("invalid configuration for header tenant extractor: " + s);
                    });
            return exchange -> match(exchange.getRequest().getHeaders().getFirst(config.get("name")));
        }
    },

    /**
     * Extract the tenant ID from the hostname. The configuration is expected to look like this:
     * <pre>
     * host-mapping:
     *   host1.example.com: tenant1
     *   host2.example.com: tenant2
     * </pre>
     */
    HOST_MAPPING {
        @Override
        public ServerWebExchangeMatcher apply(Map<String, String> config) {
            Assert.notEmpty(config, "invalid configuration for host-mapping tenant extractors require at least one mapping");
            return exchange -> match(config.get(exchange.getRequest().getURI().getHost()));
        }
    },

    /**
     * Extract the tenant ID from the hostname. The configuration is expected to look like this:
     * <pre>
     * host-pattern:
     *   pattern: (dev|stg|prd)-([a-z]+).example.com
     *   capture-group: 2
     * </pre>
     */
    HOST_PATTERN {
        @Override
        public ServerWebExchangeMatcher apply(Map<String, String> config) {
            validate(config, Set.of("pattern"), Set.of("capture-group"))
                    .ifPresent(s -> {
                        throw new IllegalArgumentException("invalid configuration for host-pattern tenant extractor: " + s);
                    });
            return exchange -> match(pattern(config).apply(exchange.getRequest().getURI().getHost()));
        }
    },

    /**
     * Extract the tenant ID from the path. The configuration is expected to look like this:
     * <pre>
     * path-pattern:
     *   pattern: /api/(v1|v2)/([a-z]+)
     *   capture-group: 2
     * </pre>
     */
    PATH_PATTERN {
        @Override
        public ServerWebExchangeMatcher apply(Map<String, String> config) {
            validate(config, Set.of("pattern"), Set.of("capture-group"))
                    .ifPresent(s -> {
                        throw new IllegalArgumentException("invalid configuration for path-pattern tenant extractor: " + s);
                    });
            return exchange -> match(pattern(config).apply(exchange.getRequest().getURI().getPath()));
        }
    };

    private static Function<String, String> pattern(Map<String, String> config) {
        var pattern = Pattern.compile(config.get("pattern"));
        var captureGroup = Integer.parseInt(config.getOrDefault("capture-group", "1"));
        return s -> {
            var matcher = pattern.matcher(s);
            if (!matcher.matches() || matcher.groupCount() < captureGroup) {
                return null;
            }

            return matcher.group(captureGroup);
        };
    }

    private static Optional<String> validate(Map<String, String> config, Set<String> required, Set<String> optional) {
        int min = required.size();
        int max = min + optional.size();

        if (config.size() < min) {
            return Optional.of("at least %d parameters are required".formatted(min));
        }

        if (config.size() > max) {
            return Optional.of("at most %d parameters are allowed".formatted(max));
        }

        for (String s : required) {
            if (!config.containsKey(s)) {
                return Optional.of("missing required parameter %s".formatted(s));
            }
        }

        for (Map.Entry<String, String> entry : config.entrySet()) {
            if (!required.contains(entry.getKey()) && !optional.contains(entry.getKey())) {
                return Optional.of("unknown parameter %s".formatted(entry.getKey()));
            }
        }

        return Optional.empty();
    }

    private static Mono<ServerWebExchangeMatcher.MatchResult> match(String tenant) {
        if (tenant == null || tenant.isBlank()) {
            return ServerWebExchangeMatcher.MatchResult.notMatch();
        }

        return ServerWebExchangeMatcher.MatchResult.match(Map.of("tenant", tenant));
    }

    @Override
    public abstract ServerWebExchangeMatcher apply(Map<String, String> config);
}
