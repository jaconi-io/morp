package io.jaconi.morp.tenant;

import static io.jaconi.morp.tenant.TenantExtractor.KEY;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;

import io.micrometer.common.KeyValues;

@Component
@ConditionalOnProperty(prefix = "morp.metrics.tenantdimension", name = "enabled", havingValue = "true")
public class TenantObservationConvention extends DefaultServerRequestObservationConvention {

	@Override
	@Nonnull
	public KeyValues getLowCardinalityKeyValues(ServerRequestObservationContext context) {
		return super.getLowCardinalityKeyValues(context).and(tenant(context));
	}

	private KeyValues tenant(ServerRequestObservationContext context) {
		return Optional.ofNullable(context.getCarrier())
				.flatMap(carrier -> TenantExtractor.extractTenant(ServerRequest.create(carrier, List.of())))
				.map(t -> KeyValues.of(KEY, t))
				.orElse(KeyValues.empty());
	}
}
