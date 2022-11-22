package io.jaconi.morp;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS;

public class MorpRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

		// Generic hints
		hints.reflection().registerType(JwtDecoder.class, INVOKE_PUBLIC_CONSTRUCTORS);

	}

}