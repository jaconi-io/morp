package io.jaconi.morp.predicates;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.cloud.gateway.server.mvc.predicate.PredicateSupplier;
import org.springframework.stereotype.Component;

@Component
public class MorpPredicateSupplier implements PredicateSupplier {

	@Override
	public Collection<Method> get() {
		return Arrays.asList(GatewayPredicates.class.getMethods());
	}

}
