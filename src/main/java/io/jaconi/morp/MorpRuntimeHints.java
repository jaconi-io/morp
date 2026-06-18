package io.jaconi.morp;

import static org.springframework.aot.hint.MemberCategory.ACCESS_DECLARED_FIELDS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS;

import org.hibernate.validator.internal.util.logging.Log_$logger;
import org.hibernate.validator.internal.util.logging.Messages_$bundle;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import io.jaconi.morp.predicates.GatewayPredicates;

public class MorpRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		// Register our own predicates
		hints.reflection().registerType(GatewayPredicates.class, INVOKE_DECLARED_METHODS);
		// Workaround for missing metadata
		// See: https://github.com/spring-projects/spring-boot/issues/50221 and https://github.com/oracle/graalvm-reachability-metadata/issues/8397
		hints.reflection().registerType(Log_$logger.class, INVOKE_DECLARED_CONSTRUCTORS, ACCESS_DECLARED_FIELDS);
		hints.reflection().registerType(Messages_$bundle.class, INVOKE_DECLARED_CONSTRUCTORS, ACCESS_DECLARED_FIELDS);
	}
}
