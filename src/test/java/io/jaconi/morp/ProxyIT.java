package io.jaconi.morp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.netty.http.client.HttpClient;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


/**
 * Integration test validating OICD roundtrips using Keycloak as IDP running in docker-compose.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "wiretap"})
@MockServerTest
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
                .headers(h -> h.set("Host", "tenant1-dev.example.com"))
                .exchange()
                .expectStatus().isOk()
                .expectAll(
                        spec -> spec.expectHeader().contentType("text/html;charset=utf-8"),
                        spec -> spec.expectCookie().exists("AUTH_SESSION_ID"),
                        spec -> spec.expectCookie().exists("KC_RESTART"))
                .expectBody().returnResult();

        System.out.println(keycloakResponse.getMethod());
        System.out.println(keycloakResponse.getUrl());
        System.out.println(keycloakResponse.getUriTemplate());
        System.out.println(new String(keycloakResponse.getResponseBody(), UTF_8));

        // TODO need to extract form target, rewrite it to localhost and post it with host header and user credentials
        // form id kc-form-login
        // form url = http://tenant1-dev.example.com/auth/realms/tenant1/login-actions/authenticate?session_code=z_SABVdMuPaSLGjG2b9uAOKcO5oFVmGa0vTnVf40R4s&amp;execution=4b7dffdf-880d-4f35-bd8e-fcecb8ec9a99&amp;client_id=morp&amp;tab_id=UZ8xNehEMSI
        // input id/name = username
        // input id/name = password

        // TODO provide user credentials and expect backend to be hit after redirects

        // make sure the mock server got the request
        /*
        mockServerClient.verify(request()
                .withMethod("GET")
                .withPath("/upstream/test"));

         */
    }
}
