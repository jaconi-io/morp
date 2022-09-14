package io.jaconi.morp.tenant;

import lombok.experimental.UtilityClass;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

@UtilityClass
public class TenantExtractor {

    public static final String KEY = "tenant";

    public static Optional<String> extractTenant(ServerWebExchange exchange) {
        var templateVariables = ServerWebExchangeUtils.getUriTemplateVariables(exchange);
        return Optional.ofNullable(templateVariables.get(KEY));
    }

}
