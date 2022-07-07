package io.jaconi.morp.oauth;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "morp.oauth2-client")
public class MorpOAuth2ClientProperties extends OAuth2ClientProperties {
}
