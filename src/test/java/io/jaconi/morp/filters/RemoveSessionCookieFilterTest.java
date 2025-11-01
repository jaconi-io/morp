package io.jaconi.morp.filters;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;

class RemoveSessionCookieFilterTest {

	@Test
	void shouldNotAddCookieIfNone() {
		var servletRequest = new MockHttpServletRequest("GET", "http://localhost:8080/get");
		servletRequest.addHeader("foo", "bar");
		var request = ServerRequest.create(servletRequest, List.of());

		RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSIONID");

		HttpHeaders headers = filter.apply(request.headers().asHttpHeaders(), request);

		assertThat(headers).containsExactly(entry("foo", List.of("bar")));
	}

	@Test
	void shouldRemoveCookieHeaderIfOnlySession() {
		var servletRequest = new MockHttpServletRequest("GET", "http://localhost:8080/get");
		servletRequest.addHeader("foo", "bar");
		servletRequest.setCookies(new Cookie("SESSION", "whatever"));
		var request = ServerRequest.create(servletRequest, List.of());

		RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSION");

		HttpHeaders headers = filter.apply(request.headers().asHttpHeaders(), request);

		assertThat(headers).containsExactly(entry("foo", List.of("bar")));
	}

	@Test
	void shouldRemoveCustomCookieHeader() {
		var servletRequest = new MockHttpServletRequest("GET", "http://localhost:8080/get");
		servletRequest.addHeader("foo", "bar");
		servletRequest.setCookies(new Cookie("CUSTOMSESSION", "whatever"));
		var request = ServerRequest.create(servletRequest, List.of());

		RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("CUSTOMSESSION");

		HttpHeaders headers = filter.apply(request.headers().asHttpHeaders(), request);

		assertThat(headers).containsExactly(entry("foo", List.of("bar")));
	}

	@Test
	void shouldRemoveSessionCookieIfOthersExist() {
		var servletRequest = new MockHttpServletRequest("GET", "http://localhost:8080/get");
		servletRequest.addHeader("foo", "bar");
		servletRequest.setCookies(new Cookie("SESSION", "whatever"), new Cookie("OTHER", "xxx"));
		var request = ServerRequest.create(servletRequest, List.of());

		RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSION");

		HttpHeaders headers = filter.apply(request.headers().asHttpHeaders(), request);

		assertThat(headers).containsExactlyInAnyOrderEntriesOf(Map.of(
				HttpHeaders.COOKIE, List.of("OTHER=xxx"),
				"foo", List.of("bar"))
		);
	}

	@Test
	void shouldNotChangeHeaderIfNoSessionCookie() {
		var servletRequest = new MockHttpServletRequest("GET", "http://localhost:8080/get");
		servletRequest.addHeader("foo", "bar");
		servletRequest.setCookies(new Cookie("ANY", "whatever"), new Cookie("OTHER", "xxx"));
		var request = ServerRequest.create(servletRequest, List.of());

		RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSIONID");

		HttpHeaders headers = filter.apply(request.headers().asHttpHeaders(), request);

		assertThat(headers).containsExactlyInAnyOrderEntriesOf(Map.of(
				HttpHeaders.COOKIE, List.of("ANY=whatever;OTHER=xxx"),
				"foo", List.of("bar"))
		);
	}
}
