package io.jaconi.morp.tenant;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ClaimConstraintsMatcher {

    public boolean matches(Map<String, Object> claims, Map<String, String> claimConstraints) {
        return claimConstraints.entrySet()
                .stream()
                .allMatch(e -> claimMatch(e.getKey(), e.getValue(), claims));
    }

    private boolean claimMatch(String constraintKey, String constraintValue, Map<String, Object> claims) {
        Object value = claims.get(constraintKey);
        if (value == null) {
            return false;
        }
        return (value.toString()).matches(constraintValue);
    }

}
