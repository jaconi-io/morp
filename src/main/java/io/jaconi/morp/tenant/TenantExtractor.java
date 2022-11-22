package io.jaconi.morp.tenant;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;

@UtilityClass
public class TenantExtractor {

    public static final String KEY = "tenant";

    @SuppressWarnings("unchecked")
    public static Optional<String> extractTenant(Map<String, Object> attributes) {
        var templateVariables = (Map<String, String>) attributes.getOrDefault(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE, new HashMap<>());
        return Optional.ofNullable(templateVariables.get(KEY));
    }

}
