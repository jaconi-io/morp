package io.jaconi.morp.filters;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RemoveSessionCookieFilter implements HttpHeadersFilter {

    final String cookieName;

    @Override
    public HttpHeaders filter(HttpHeaders input, ServerWebExchange exchange) {
        HttpHeaders filtered = new HttpHeaders();

        for (Map.Entry<String, List<String>> entry : input.headerSet()) {
            if (HttpHeaders.COOKIE.equals(entry.getKey())) {
                continue;
            }
            filtered.addAll(entry.getKey(), entry.getValue());
        }
        String filteredCookies = exchange.getRequest().getCookies().values()
                .stream()
                .flatMap(Collection::stream)
                .filter(c -> !cookieName.equals(c.getName()))
                .map(HttpCookie::toString)
                .collect(Collectors.joining(";"));

        if (StringUtils.isNotBlank(filteredCookies)) {
            filtered.set(HttpHeaders.COOKIE, filteredCookies);
        }
        return filtered;
    }
}
