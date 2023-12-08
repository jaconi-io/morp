package io.jaconi.morp;

import static io.jaconi.morp.oauth.ProxyAuthorityMapper.ROLE_PROXY;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;

import io.jaconi.morp.filters.RemoveSessionCookieFilter;
import io.jaconi.morp.filters.TenantExtractionFilter;

@Configuration
public class SecurityConfiguration {

	private static final String DEBUG_ENDPOINT = "/debug";

	private static final NegatedRequestMatcher TENANT_EXTRACTION_REQUEST_MATCHER = new NegatedRequestMatcher(
			new OrRequestMatcher(
					new AntPathRequestMatcher(DEBUG_ENDPOINT),
					new AntPathRequestMatcher("/oauth2/**"),
					new AntPathRequestMatcher("/login/oauth2/**"),
					EndpointRequest.toAnyEndpoint()
			)
	);

	@Bean
	SecurityFilterChain securityFilterChain(AuthenticationEntryPoint authenticationEntryPoint, HttpSecurity httpSecurity,
			RouterFunctionMapping routerFunctionMapping) throws Exception {
		return httpSecurity
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authorizeExchangeSpec ->
						authorizeExchangeSpec.requestMatchers(DEBUG_ENDPOINT).authenticated()
								.requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
								.anyRequest().hasAuthority(ROLE_PROXY)
				)
				.oauth2Login(oAuth2LoginSpec -> {
				})
				.oauth2Client(oAuth2ClientSpec -> {
				})
				.exceptionHandling(exceptionHandlingSpec ->
						exceptionHandlingSpec.authenticationEntryPoint(authenticationEntryPoint))
				.logout(logoutSpec -> {
				})
				.addFilterAfter(new TenantExtractionFilter(TENANT_EXTRACTION_REQUEST_MATCHER, routerFunctionMapping),
						LogoutFilter.class)
				.build();
	}

	@Bean
	public RemoveSessionCookieFilter removeSessionCookie(ServerProperties serverProperties) {
		return new RemoveSessionCookieFilter(serverProperties.getServlet().getSession().getCookie().getName());
	}

}
