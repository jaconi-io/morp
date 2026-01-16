package io.jaconi.morp.filters;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.server.mvc.filter.HttpHeadersFilter;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.function.ServerRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RemoveSessionCookieFilter implements HttpHeadersFilter.RequestHttpHeadersFilter {

    final String cookieName;

    @Override
    public HttpHeaders apply(HttpHeaders input, ServerRequest request) {
        HttpHeaders filtered = new HttpHeaders();

        for (Map.Entry<String, List<String>> entry : input.headerSet()) {
            if (HttpHeaders.COOKIE.equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            filtered.addAll(entry.getKey(), entry.getValue());
        }
        String filteredCookies = request.cookies().values()
                .stream()
                .flatMap(Collection::stream)
                .filter(c -> !cookieName.equals(c.getName()))
                .map(c-> new HttpCookie(c.getName(), c.getValue()))
                .map(HttpCookie::toString)
                .collect(Collectors.joining(";"));

        if (StringUtils.isNotBlank(filteredCookies)) {
            filtered.set(HttpHeaders.COOKIE, filteredCookies);
        }
        return filtered;
    }
}
