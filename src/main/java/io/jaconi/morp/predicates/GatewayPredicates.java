package io.jaconi.morp.predicates;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.springframework.cloud.gateway.server.mvc.common.MvcUtils;
import org.springframework.cloud.gateway.server.mvc.common.Shortcut;
import org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates;
import org.springframework.cloud.gateway.server.mvc.predicate.PredicateSupplier;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.util.UriComponentsBuilder;

public class GatewayPredicates {

	public static final String KEY = "tenant";

	@Shortcut
	// Only one pattern supported for now
	public static RequestPredicate tenantFromHost(String tenant, String pattern) {
		return GatewayRequestPredicates.host(pattern)
				.and(request -> {
					MvcUtils.putUriTemplateVariables(request, Map.of(KEY, tenant));
					return true;
				});
	}

	@Shortcut
	public static RequestPredicate tenantFromHeader(String header) {
		return GatewayRequestPredicates.header(header)
				.and(request -> {
					var headerValue = request.headers().firstHeader(header);
					MvcUtils.putUriTemplateVariables(request, Map.of(KEY, headerValue));
					return true;
				});
	}

	public static class MorpPredicateSupplier implements PredicateSupplier {

		@Override
		public Collection<Method> get() {
			return Arrays.asList(GatewayPredicates.class.getMethods());
		}

	}

}
