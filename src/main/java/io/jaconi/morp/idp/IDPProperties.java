package io.jaconi.morp.idp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "idp")
public record IDPProperties(FromTenant fromTenant) {

    @ConfigurationProperties(prefix = "from-tenant")
    public record FromTenant(Pattern pattern, int captureGroup, Map<String, String> mapping) {
        public FromTenant(Pattern pattern, int captureGroup, Map<String, String> mapping) {
            Assert.isTrue(captureGroup >= 0, "the capture group must be greater than or equal to 0");

            this.pattern = pattern;
            this.captureGroup = captureGroup;
            this.mapping = mapping == null ? Collections.emptyMap() : mapping;
        }
    }
}
