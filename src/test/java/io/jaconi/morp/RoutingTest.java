package io.jaconi.morp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.springtest.MockServerTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.netty.http.client.HttpClient;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "wiretap"})
@MockServerTest
public class RoutingTest {

    @LocalServerPort
    private int port;

    private MockServerClient mockServerClient;

    private WebTestClient client;

    @BeforeEach
    void setUp() {

        // control the HTTP client to enable wiretap logs
        HttpClient httpClient = HttpClient.create()
                .wiretap(true) // hex dump wiretap
                .compress(true);

        // bind the client to the gateway running on a random port
        client = WebTestClient.bindToServer(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://localhost:" + port)
                .build();

        mockServerClient.reset();
    }

    @Test
    void testUnroutedPath() {
        // expect HTTP 404 for an unknown path (a path without route)
        client.get()
                .uri("/not-existing-path")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testRoutedPathWithoutTenant() {
        // expect some gateway error code if no tenant can be extracted
        client.get()
                .uri("/routed-without-tenant")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void testRoutedPathWithTenant() {
        // simulate an upstream using mockserver
        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withPath("/routed-with-tenant"))
                .respond(response()
                        .withStatusCode(200));

        // expect redirect to login
        client.get()
                .uri("/routed-with-tenant/tenant1")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/oauth2/authorization/tenant1");
    }
}
