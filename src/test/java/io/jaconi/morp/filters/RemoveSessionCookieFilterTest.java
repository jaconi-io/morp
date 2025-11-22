package io.jaconi.morp.filters;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class RemoveSessionCookieFilterTest {

    @Test
    void shouldNotAddCookieIfNone() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/get")
                .header("foo", "bar")
                .build();

        RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSIONID");

        HttpHeaders headers = filter.filter(request.getHeaders(), MockServerWebExchange.from(request));

        assertThat(headers.toSingleValueMap()).containsExactly(entry("foo", "bar"));
    }

    @Test
    void shouldRemoveCookieHeaderIfOnlySession() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/get")
                .header("foo", "bar")
                .cookie(new HttpCookie("SESSION", "whatever")).build();

        RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSION");

        HttpHeaders headers = filter.filter(request.getHeaders(), MockServerWebExchange.from(request));

        assertThat(headers.toSingleValueMap()).containsExactly(entry("foo", "bar"));
    }

    @Test
    void shouldRemoveCustomCookieHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/get")
                .header("foo", "bar")
                .cookie(new HttpCookie("CUSTOMSESSION", "whatever")).build();

        RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("CUSTOMSESSION");

        HttpHeaders headers = filter.filter(request.getHeaders(), MockServerWebExchange.from(request));

        assertThat(headers.toSingleValueMap()).containsExactly(entry("foo", "bar"));
    }

    @Test
    void shouldRemoveSessionCookieIfOthersExist() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/get")
                .header("foo", "bar")
                .cookie(new HttpCookie("SESSION", "whatever"), new HttpCookie("OTHER", "xxx")).build();

        RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSION");

        HttpHeaders headers = filter.filter(request.getHeaders(), MockServerWebExchange.from(request));

        assertThat(headers.toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                HttpHeaders.COOKIE, "OTHER=xxx", "foo", "bar")
        );
    }

    @Test
    void shouldNotChangeHeaderIfNoSessionCookie() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/get")
                .header("foo", "bar")
                .cookie(new HttpCookie("ANY", "whatever"), new HttpCookie("OTHER", "xxx")).build();

        RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSIONID");

        HttpHeaders headers = filter.filter(request.getHeaders(), MockServerWebExchange.from(request));

        assertThat(headers.toSingleValueMap()).containsExactlyInAnyOrderEntriesOf(Map.of(
                HttpHeaders.COOKIE, "ANY=whatever;OTHER=xxx", "foo", "bar")
        );
    }
}
