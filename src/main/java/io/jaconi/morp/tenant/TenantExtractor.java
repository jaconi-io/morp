package io.jaconi.morp.tenant;

import java.util.Optional;

import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.web.servlet.function.ServerRequest;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TenantExtractor {

	public static final String KEY = "tenant";

	public static Optional<String> extractTenant(ServerRequest request) {
		if (MvcUtils.getUriTemplateVariables(request).containsKey(KEY)) {
			String tenant = (String) MvcUtils.getUriTemplateVariables(request).get(KEY);
			return Optional.ofNullable(tenant);
		}
		return Optional.empty();
	}

}
