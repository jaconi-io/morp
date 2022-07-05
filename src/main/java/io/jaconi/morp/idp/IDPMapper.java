package io.jaconi.morp.idp;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class IDPMapper {
    private final IDPProperties idpProperties;

    public IDPMapper(IDPProperties idpProperties) {
        this.idpProperties = idpProperties;
    }

    public Mono<String> fromTenant(String tenant) {
        return fromMapping(tenant).switchIfEmpty(fromPattern(tenant));
    }

    private Mono<String> fromPattern(String tenant) {
        var matcher = idpProperties.fromTenant().pattern().matcher(tenant);
        var captureGroup = idpProperties.fromTenant().captureGroup();

        if (!matcher.matches() || matcher.groupCount() < captureGroup) {
            return Mono.empty();
        }

        return Mono.fromSupplier(() -> matcher.group(captureGroup));
    }

    private Mono<String> fromMapping(String tenant) {
        return Mono.justOrEmpty(idpProperties.fromTenant().mapping().get(tenant));
    }
}
