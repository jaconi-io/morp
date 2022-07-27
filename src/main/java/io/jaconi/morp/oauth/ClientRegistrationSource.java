package io.jaconi.morp.oauth;

import lombok.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.builder.*;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;


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

    public String sha256() {
        return DigestUtils.sha256Hex(toString());
    }
}
