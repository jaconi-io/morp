package io.jaconi.morp;

import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import io.jaconi.morp.predicates.GatewayPredicates;

public class MorpRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		// Register our own predicates
		hints.reflection().registerType(GatewayPredicates.class, INVOKE_DECLARED_METHODS);
	}
}
