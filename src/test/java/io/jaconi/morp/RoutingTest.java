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
    void testRoutedPathWithoutBackend() {
        // expect some gateway error code if the backend is missing
        client.get()
                .uri("/routed-without-backend")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void testRoutedPathWithBackend() {
        // simulate an upstream using mockserver
        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withPath("/routed-with-backend"))
                .respond(response()
                        .withStatusCode(200));

        // expect ok response from backend
        client.get()
                .uri("/routed-with-backend")
                .exchange()
                .expectStatus().isOk();
    }
}
