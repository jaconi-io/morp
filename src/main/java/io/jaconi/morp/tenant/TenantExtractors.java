package io.jaconi.morp.tenant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class TenantExtractors implements Function<ServerWebExchange, String> {
    private final List<Function<ServerWebExchange, String>> tenantExtractors;

    public TenantExtractors(@Value("${tenant-extractor.extractors}") List<Map<String, Map<String, String>>> extractors) {
        this.tenantExtractors = extractors.stream()
                .map(config -> {
                    Assert.isTrue(config.size() == 1, "expected exactly one tenant extractor type");

                    var type = config.keySet().iterator().next();
                    return switch (type) {
                        case "header" -> header(config.get(type));
                        case "host-mapping" -> hostMapping(config.get(type));
                        case "host-pattern" -> hostPattern(config.get(type));
                        case "path-pattern" -> pathPattern(config.get(type));
                        default -> throw new IllegalArgumentException("unknown tenant extractor type: " + type);
                    };
                })
                .collect(Collectors.toList());
    }

    @Override
    public String apply(ServerWebExchange exchange) {
        return tenantExtractors.stream()
                .map(extractor -> extractor.apply(exchange))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Function<ServerWebExchange, String> header(Map<String, String> config) {
        return exchange -> exchange.getRequest().getHeaders().getFirst(config.get("name"));
    }

    private Function<ServerWebExchange, String> hostMapping(Map<String, String> config) {
        return exchange -> config.get(exchange.getRequest().getURI().getHost());
    }

    private Function<ServerWebExchange, String> hostPattern(Map<String, String> config) {
        return pattern(config).compose(exchange -> exchange.getRequest().getURI().getHost());
    }

    public Function<ServerWebExchange, String> pathPattern(Map<String, String> config) {
        return pattern(config).compose(exchange -> exchange.getRequest().getURI().getPath());
    }

    private Function<String, String> pattern(Map<String, String> config) {
        var pattern = Pattern.compile(config.get("pattern"));
        var captureGroup = Integer.parseInt(config.get("capture-group"));
        return s -> {
            var matcher = pattern.matcher(s);
            if (!matcher.matches() || matcher.groupCount() < captureGroup) {
                return null;
            }

            return matcher.group(captureGroup);
        };
    }
}
