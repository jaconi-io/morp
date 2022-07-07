package io.jaconi.morp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.netty.http.client.HttpClient;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


/**
 * Integration test validating OICD roundtrips using Keycloak as IDP running in docker-compose.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "wiretap"})
@MockServerTest
@Tag("integration")
public class ProxyIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private MockServerClient mockServerClient;

    private WebTestClient client;

    @BeforeEach
    void setUp() {

        // control the HTTP client to enable wiretap logs
        HttpClient httpClient = HttpClient.create()
                .followRedirect(true) // follow OIDC redirects automatically
                .wiretap(true) // hex dump wiretap
                .compress(true);

        client = WebTestClient.bindToServer(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://localhost:" + port)
                .build();

        mockServerClient.reset();
    }


    @Test
    void testKeycloak() {

        // simulate an upstream using mockserver
        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withPath("/upstream/.+"))
                .respond(response()
                        .withStatusCode(200)
                        .withBody("I am the backend"));

        // run matching request via morp as 'tenant1' - 'test' profile has tenant1 authenticate via Keycloak
        // we should see a login mask after following several redirects
        var keycloakResponse = client.get()
                .uri("/upstream/test")
                .header("x-tenant-id", "tenant1")
                .exchange()
                .expectStatus().isOk()
                .expectAll(
                        spec -> spec.expectHeader().contentType("text/html;charset=utf-8"),
                        spec -> spec.expectCookie().exists("AUTH_SESSION_ID"),
                        spec -> spec.expectCookie().exists("KC_RESTART"))
                .expectBody().returnResult();

        // extract the Keycloak login form HTML
        var html = new String(keycloakResponse.getResponseBody(), UTF_8);
        assertThat(html).contains("kc-form-login");

        // extract the login form post target URL (that holds all the oidc state)
        var url = Jsoup.parse(html)
                .select("form#kc-form-login")
                .attr("action");

        // fill the login form with our known test user credentials
        // TODO An expected CSRF token cannot be found
        var cookies = keycloakResponse.getResponseCookies().toSingleValueMap();
        client.post()
                .uri(url)
                .cookies(c -> cookies.entrySet().stream().forEach(e -> c.add(e.getKey(), e.getValue().getValue())))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("username", "test@jaconi.io")
                        .with("password", "password")
                        .with("credentialId", "")
                        .with("login", "Sign In"))
                .exchange()
                .expectStatus().is3xxRedirection();


        // make sure the mock server got the request
        mockServerClient.verify(request()
                .withMethod("GET")
                .withPath("/upstream/test"));
    }
}
