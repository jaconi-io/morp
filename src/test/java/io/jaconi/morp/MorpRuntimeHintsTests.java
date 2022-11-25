package io.jaconi.morp;

import com.sun.management.OperatingSystemMXBean;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;

class MorpRuntimeHintsTests {

    @Test
    void shouldRegisterHints() {
        RuntimeHints hints = new RuntimeHints();
        new MorpRuntimeHints().registerHints(hints, getClass().getClassLoader());

        // Generic hints
        assertThat(reflection().onType(OperatingSystemMXBean.class)).accepts(hints);
        assertThat(reflection().onType(JwtDecoder.class).withMemberCategory(INVOKE_PUBLIC_CONSTRUCTORS)).accepts(hints);

    }

}