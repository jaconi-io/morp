package io.jaconi.morp.filters;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

class RemoveSessionCookieFilterTest {

    @Test
    public void shouldNotAddCookieIfNone() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/get")
                .header("foo", "bar")
                .build();

        RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSIONID");

        HttpHeaders headers = filter.filter(request.getHeaders(), MockServerWebExchange.from(request));

        assertThat(headers).containsExactly(entry("foo", List.of("bar")));
    }

    @Test
    public void shouldRemoveCookieHeaderIfOnlySession() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/get")
                .header("foo", "bar")
                .cookie(new HttpCookie("SESSION", "whatever")).build();

        RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSION");

        HttpHeaders headers = filter.filter(request.getHeaders(), MockServerWebExchange.from(request));

        assertThat(headers).containsExactly(entry("foo", List.of("bar")));
    }

    @Test
    public void shouldRemoveCustomCookieHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/get")
                .header("foo", "bar")
                .cookie(new HttpCookie("CUSTOMSESSION", "whatever")).build();

        RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("CUSTOMSESSION");

        HttpHeaders headers = filter.filter(request.getHeaders(), MockServerWebExchange.from(request));

        assertThat(headers).containsExactly(entry("foo", List.of("bar")));
    }

    @Test
    public void shouldRemoveSessionCookieIfOthersExist() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/get")
                .header("foo", "bar")
                .cookie(new HttpCookie("SESSION", "whatever"), new HttpCookie("OTHER", "xxx")).build();

        RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSION");

        HttpHeaders headers = filter.filter(request.getHeaders(), MockServerWebExchange.from(request));

        assertThat(headers).containsExactlyInAnyOrderEntriesOf(Map.of(
                HttpHeaders.COOKIE, List.of("OTHER=xxx"),
                "foo", List.of("bar"))
        );
    }

    @Test
    public void shouldNotChangeHeaderIfNoSessionCookie() {
        MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/get")
                .header("foo", "bar")
                .cookie(new HttpCookie("ANY", "whatever"), new HttpCookie("OTHER", "xxx")).build();

        RemoveSessionCookieFilter filter = new RemoveSessionCookieFilter("SESSIONID");

        HttpHeaders headers = filter.filter(request.getHeaders(), MockServerWebExchange.from(request));

        assertThat(headers).containsExactlyInAnyOrderEntriesOf(Map.of(
                HttpHeaders.COOKIE, List.of("ANY=whatever;OTHER=xxx"),
                "foo", List.of("bar"))
        );
    }
}