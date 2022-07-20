package io.jaconi.morp;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.springtest.MockServerTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.netty.http.client.HttpClient;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;


/**
 * Integration test validating a complete OIDC (happy path) round trip using Keycloak as IDP running in docker-compose.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
@MockServerTest
public class ProxyIT {

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


        // step 1 - run a request for our upstream (using a tenant that will be mapped to Keycloak)
        // expect a 302 redirect into the Spring OAuth2 registry with the identified tenant
        var step1 = client.get()
                .uri("/upstream/test")
                .accept(MediaType.TEXT_HTML)
                .header("x-tenant-id", "tenant1")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/oauth2/authorization/tenant1")
                .expectCookie().exists("SESSION")
                .expectBody().returnResult();

        // memorize the session with the gateway
        var session = step1.getResponseCookies().getFirst("SESSION").getValue();

        // step 2 - follow the Spring OAuth2 redirect
        // expect the actual IDP redirect into the proper Keycloak realm
        var step2 = client.get()
                .uri(step1.getResponseHeaders().getLocation().getPath())
                .cookie("SESSION", session)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("location", ".*/realms/tenant1/protocol/openid-connect/.*")
                .expectBody().returnResult();

        // step 3 - follow the IDP redirect
        // expect the Keycloak login mask
        var step3 = client.get()
                .uri(step2.getResponseHeaders().getLocation())
                .exchange()
                .expectStatus().isOk()
                .expectBody().returnResult();

        // extract the login form HTML
        var html = new String(step3.getResponseBody(), UTF_8);
        assertThat(html).contains("kc-form-login");

        // extract the login form post target URL (that holds all the oidc state)
        var url = Jsoup.parse(html)
                .select("form#kc-form-login")
                .attr("action");

        // step 4 - fill the form with test user credentials
        // expect redirection back to gateway into OAuth2 authorization code flow for correct tenant
        var step4 = client.post()
                .uri(url)
                .cookies(c -> step3.getResponseCookies().toSingleValueMap().entrySet().stream().forEach(e -> c.add(e.getKey(), e.getValue().getValue())))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(fromFormData("username", "test@jaconi.io").with("password", "password"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("location", ".*/login/oauth2/code/tenant1.*")
                .expectCookie().exists("KEYCLOAK_IDENTITY")
                .expectCookie().exists("KEYCLOAK_SESSION")
                .expectBody().returnResult();

        // step 5 - follow the authorization code flow redirect back to the gateway
        // expect to be redirected to the upstream request url with a new SESSION cookie set
        var step5 = client.get()
                .uri(step4.getResponseHeaders().getLocation())
                .cookie("SESSION", session)
                .header("x-tenant-id", "tenant1")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/upstream/test")
                .expectCookie().exists("SESSION")
                .expectBody().returnResult();

        // extract the new session cookie (not the auth session anymore)
        session = step5.getResponseCookies().getFirst("SESSION").getValue();

        // step 6 - follow the upstream redirect to finally execute the request we started with
        // expect our upstream response
        var step6 = client.get()
                .uri(step5.getResponseHeaders().getLocation().getPath())
                .cookie("SESSION", session)
                .header("x-tenant-id", "tenant1")
                .exchange()
                .expectStatus().isOk()
                .expectBody().returnResult();

        var bodyString = new String(step6.getResponseBody(), UTF_8);
        assertThat(bodyString).isEqualTo("I am the backend");
    }
}
