package io.jaconi.morp.oauth;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jaconi.oidc-proxy.oauth2.client")
public class JaconiOAuth2ClientProperties extends OAuth2ClientProperties {
}
