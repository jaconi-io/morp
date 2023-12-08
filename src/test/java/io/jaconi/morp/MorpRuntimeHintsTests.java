package io.jaconi.morp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

import io.jaconi.morp.predicates.GatewayPredicates;

class MorpRuntimeHintsTests {

    @Test
    void shouldRegisterHints() {
        RuntimeHints hints = new RuntimeHints();
        new MorpRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(reflection().onType(GatewayPredicates.class)).accepts(hints);

    }

}