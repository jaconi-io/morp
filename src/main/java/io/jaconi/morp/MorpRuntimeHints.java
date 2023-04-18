package io.jaconi.morp;

import io.netty.channel.epoll.EpollDatagramChannel;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.ClassUtils;

import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_PUBLIC_METHODS;

public class MorpRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        // Generic hints
        hints.reflection().registerType(ClassUtils.resolveClassName("com.sun.management.OperatingSystemMXBean", classLoader), INVOKE_PUBLIC_METHODS);
        hints.reflection().registerType(JwtDecoder.class, INVOKE_PUBLIC_CONSTRUCTORS);

        // This should not be required anymore as https://github.com/netty/netty/pull/13279 is contained in netty
        // 4.1.91.Final (which is used by Spring Boot 3.0.5). However, things go south without it, so I will keep it
        // for now.
        hints.reflection().registerType(EpollDatagramChannel.class, INVOKE_DECLARED_CONSTRUCTORS);
    }
}
