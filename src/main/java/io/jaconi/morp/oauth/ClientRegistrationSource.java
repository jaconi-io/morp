package io.jaconi.morp.oauth;

import lombok.*;
import org.apache.commons.lang3.builder.*;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientRegistrationSource {

    private OAuth2ClientProperties.Provider provider;

    @NonNull
    private OAuth2ClientProperties.Registration registration;

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o, false, null, true);
    }

    @Override
    public int hashCode() {
        int result = provider != null ? HashCodeBuilder.reflectionHashCode(provider) : 0;
        result = 31 * result + HashCodeBuilder.reflectionHashCode(registration);
        return result;
    }

    public String toString() {
        return "ClientRegistrationSource(provider=" + ToStringBuilder.reflectionToString(this.getProvider(), ToStringStyle.SHORT_PREFIX_STYLE)
                + ", registration=" + new ReflectionToStringBuilder(this.getRegistration(), ToStringStyle.SHORT_PREFIX_STYLE).setExcludeFieldNames("clientId", "clientSecret").toString()
                + ")";
    }

	@SneakyThrows(NoSuchAlgorithmException.class)
    public String sha256() {
		var messageDigest = MessageDigest.getInstance("SHA-256");
		var hash = messageDigest.digest(toString().getBytes(StandardCharsets.UTF_8));
		return HexFormat.of().formatHex(hash);
    }
}
