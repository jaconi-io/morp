package io.jaconi.morp.tenant;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ClaimConstraintsMatcher {

    public boolean matches(Map<String, Object> claims, Map<String, List<String>> claimConstraints) {
        return claimConstraints.entrySet()
                .stream()
                .allMatch(e -> claimMatch(e.getKey(), e.getValue(), claims));
    }

    private boolean claimMatch(String constraintKey, List<String> constraintValue, Map<String, Object> claims) {
        var value = claims.get(constraintKey);
        if (value == null) {
            return false;
        }

        return constraintValue.stream().anyMatch(value.toString()::matches);
    }
}
