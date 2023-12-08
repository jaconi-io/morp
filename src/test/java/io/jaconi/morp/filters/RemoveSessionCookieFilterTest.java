package io.jaconi.morp.filters;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.function.ServerRequest;

import jakarta.servlet.http.Cookie;

class RemoveSessionCookieFilterTest {

	@Test
	void shouldNotAddCookieIfNone() {
		var servletRequest = new MockHttpServletRequest("GET", "http://localhost:8080/get");
		servletRequest.addHeader("foo", "bar");
		var request = ServerRequest.create(servletRequest, List.of());

		RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSIONID");

		HttpHeaders headers = filter.apply(request.headers().asHttpHeaders(), request);

		assertThat(headers.toSingleValueMap()).containsExactly(entry("foo", "bar"));
	}

	@Test
	void shouldRemoveCookieHeaderIfOnlySession() {
		var servletRequest = new MockHttpServletRequest("GET", "http://localhost:8080/get");
		servletRequest.addHeader("foo", "bar");
		servletRequest.setCookies(new Cookie("SESSION", "whatever"));
		var request = ServerRequest.create(servletRequest, List.of());

		RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSION");

		HttpHeaders headers = filter.apply(request.headers().asHttpHeaders(), request);

		assertThat(headers.toSingleValueMap()).containsExactly(entry("foo", "bar"));
	}

	@Test
	void shouldRemoveCustomCookieHeader() {
		var servletRequest = new MockHttpServletRequest("GET", "http://localhost:8080/get");
		servletRequest.addHeader("foo", "bar");
		servletRequest.setCookies(new Cookie("CUSTOMSESSION", "whatever"));
		var request = ServerRequest.create(servletRequest, List.of());

		RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("CUSTOMSESSION");

		HttpHeaders headers = filter.apply(request.headers().asHttpHeaders(), request);

		assertThat(headers.toSingleValueMap()).containsExactly(entry("foo", "bar"));
	}

	@Test
	void shouldRemoveSessionCookieIfOthersExist() {
		var servletRequest = new MockHttpServletRequest("GET", "http://localhost:8080/get");
		servletRequest.addHeader("foo", "bar");
		servletRequest.setCookies(new Cookie("SESSION", "whatever"), new Cookie("OTHER", "xxx"));
		var request = ServerRequest.create(servletRequest, List.of());

		RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSION");

		HttpHeaders headers = filter.apply(request.headers().asHttpHeaders(), request);

		assertThat(headers.toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
				HttpHeaders.COOKIE, "OTHER=xxx",
				"foo", "bar")
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

		assertThat(headers.toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
				HttpHeaders.COOKIE, "ANY=whatever;OTHER=xxx",
				"foo", "bar")
		);
	}
}
