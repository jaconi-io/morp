package io.jaconi.morp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.aot.hint.MemberCategory.ACCESS_DECLARED_FIELDS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;

import org.hibernate.validator.internal.util.logging.Log_$logger;
import org.hibernate.validator.internal.util.logging.Messages_$bundle;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;

import io.jaconi.morp.predicates.GatewayPredicates;

class MorpRuntimeHintsTests {

    @Test
    void shouldRegisterHints() {
        RuntimeHints hints = new RuntimeHints();
        new MorpRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(reflection().onType(GatewayPredicates.class)).accepts(hints);
        assertThat(hints.reflection().getTypeHint(Log_$logger.class).getMemberCategories())
                .contains(INVOKE_DECLARED_CONSTRUCTORS, ACCESS_DECLARED_FIELDS);
        assertThat(hints.reflection().getTypeHint(Messages_$bundle.class).getMemberCategories())
                .contains(INVOKE_DECLARED_CONSTRUCTORS, ACCESS_DECLARED_FIELDS);

    }

}
