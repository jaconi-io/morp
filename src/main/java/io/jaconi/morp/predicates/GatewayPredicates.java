package io.jaconi.morp.predicates;

import java.util.Arrays;
import java.util.Map;

import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.cloud.gateway.server.mvc.common.Shortcut;
import org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates;
import org.springframework.util.Assert;
import org.springframework.web.servlet.function.RequestPredicate;

public interface GatewayPredicates {

	String KEY = "tenant";

	@Shortcut(type = Shortcut.Type.LIST)
	static RequestPredicate tenantFromHost(String... tokens) {
		Assert.isTrue(tokens.length >= 2 , "At least the tenant and one host pattern must be supplied.");
		var tenant = tokens[0];
		var patterns = Arrays.copyOfRange(tokens, 1, tokens.length);
		return GatewayRequestPredicates.host(patterns)
				.and(request -> {
					MvcUtils.putUriTemplateVariables(request, Map.of(KEY, tenant));
					return true;
				});
	}

	@Shortcut
	static RequestPredicate tenantFromHeader(String header) {
		return GatewayRequestPredicates.header(header)
				.and(request -> {
					var headerValue = request.headers().firstHeader(header);
					MvcUtils.putUriTemplateVariables(request, Map.of(KEY, headerValue));
					return true;
				});
	}

}
