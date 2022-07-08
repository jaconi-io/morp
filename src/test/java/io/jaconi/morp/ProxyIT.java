package io.jaconi.morp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

/**
 * Integration test validating OICD roundtrips using Keycloak as IDP running in docker-compose.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "wiretap"})
@AutoConfigureWebTestClient
@MockServerTest
public class ProxyIT {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private MockServerClient mockServerClient;

    @Autowired
    private WebTestClient client;

    @BeforeEach
    void setUp() {

        /*
        // control the HTTP client to enable wiretap logs
        HttpClient httpClient = HttpClient.create()
                .wiretap(true) // hex dump wiretap
                .compress(true);

        client = WebTestClient.bindToServer(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://localhost:" + port)
                .build();
        */

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

        // access upstream without credentials and expect a OIDC redirection into our tenant specific registration
        client.get()
                .uri("/upstream/test")
                .header("x-tenant-id", "tenant1")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/oauth2/authorization/tenant1");

        // access upstream with a mocked token
        this.client.mutateWith(mockOidcLogin().idToken(token -> token.claim("name", "Mock User")))
                .get()
                .uri("/upstream/test")
                .header("x-tenant-id", "tenant1")
                .exchange()
                .expectStatus().is3xxRedirection();


        // TODO verify the request that upstream has seen
        /*
        mockServerClient.verify(request()
                .withMethod("GET")
                .withPath("/upstream/test"));
        */
    }

    @Test
    void testWithUnknownTenant() {

        // try to access our upstream with an unsupported tenant
        client.get()
                .uri("/upstream/test")
                .header("x-tenant-id", "foobar")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/oauth2/authorization/foobar");

        // TODO we are currently getting redirected to Keycloak 'tenant1' realm - not sure this is what we want
        // -> we likely want this to be a test for the tenant extractor to restrict the acceptable tenants
        client.get()
                .uri("/oauth2/authorization/foobar")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valuesMatch("location", ".*/auth/realms/tenant1/.*");
    }
}
