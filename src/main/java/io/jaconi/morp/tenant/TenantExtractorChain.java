package io.jaconi.morp.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@ConstructorBinding
@ConfigurationProperties(prefix = "morp")
public class TenantExtractorChain implements ServerWebExchangeMatcher {
    private final List<ServerWebExchangeMatcher> tenantExtractors;

    public TenantExtractorChain(ArrayList<Map<TenantExtractor, Map<String, String>>> tenantExtractors) {
        // Convert the morp.tenant-extractors configuration to actual tenant extractors.
        this.tenantExtractors = tenantExtractors.stream()
                .map(config -> {
                    Assert.isTrue(config.size() == 1, "expected exactly one tenant extractor type");
                    var type = config.keySet().iterator().next();
                    return type.apply(config.get(type));
                })
                .collect(Collectors.toList());

        if (tenantExtractors.isEmpty()) {
            log.warn("at least one tenant extractor is required to use multi-tenancy features");
        }
    }

    @Override
    public Mono<MatchResult> matches(ServerWebExchange exchange) {
        return Flux.concat(tenantExtractors.stream().map(matcher -> matcher.matches(exchange)).collect(Collectors.toList()))
                .filter(MatchResult::isMatch)
                .next();
    }
}
