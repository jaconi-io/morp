package io.jaconi.morp;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.mockserver.client.MockServerClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.DockerLoggerFactory;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Integration test base class that sets up Morp, Keycloak and MockServer (as programmable backend) via
 * TestContainers. Integration tests running against Morp are expected to extend this base class so that the container
 * setup can be shared across test executions. Container cleanup is done by TestContainers at the end of the JVM test
 * run (by a cleanup container).
 */
public class TestBase {

    protected static Network network;
    protected static KeycloakContainer keycloakContainer;
    protected static MockServerContainer mockserverContainer;
    protected static GenericContainer morpContainer;

    protected static MockServerClient mockServerClient;

    protected static WebTestClient webTestClient;

    static {

        // we need a network for our containers to see each other
        network = Network.newNetwork();

        // start Keycloak in that network
        keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:18.0.2")
                .withRealmImportFile("/keycloak/realm-1.json")
                .withNetwork(network)
                .withExposedPorts(8080)
                .withNetworkAliases("keycloak")
                .withEnv("KC_HOSTNAME_STRICT", "false")
                .withEnv("KC_HOSTNAME_STRICT_HTTPS", "false")
                .withEnv("KC_PROXY", "edge")
                .withReuse(true);
        keycloakContainer.start();

        // start mockserver as generic upstream (protected by Morp)
        mockserverContainer = new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"))
                .withNetwork(network)
                .withNetworkAliases("upstream")
                .withReuse(true);
        mockserverContainer
                .withLogConsumer(new Slf4jLogConsumer(DockerLoggerFactory.getLogger(mockserverContainer.getDockerImageName())))
                .start();

        // point our mockserver client to mockserver running in the container
        mockServerClient = new MockServerClient(mockserverContainer.getHost(), mockserverContainer.getServerPort());

        // start Morp (auth proxy) in that network
        morpContainer = new GenericContainer(DockerImageName.parse("ghcr.io/jaconi-io/morp:latest"))
                .withNetwork(network)
                .withNetworkAliases("morp", "tenant1-morp")
                .withExposedPorts(8081, 8082)
                .withEnv("SPRING_PROFILES_ACTIVE", "test")
                .withReuse(true)
                .withFileSystemBind(
                        "./src/integrationTest/resources/application-test.yaml",
                        "/workspace/config/application-test.yaml",
                        BindMode.READ_ONLY)
                .waitingFor(new HttpWaitStrategy()
                        .forPort(8082)
                        .forPath("/actuator/health/readiness")
                        .withStartupTimeout(Duration.ofMinutes(5)));
        morpContainer
                .withLogConsumer(new Slf4jLogConsumer(DockerLoggerFactory.getLogger(morpContainer.getDockerImageName())))
                .start();

        // control the HTTP client to enable wiretap logs
        HttpClient httpClient = HttpClient.create()
                .wiretap(true) // hex dump wiretap
                .compress(true);

        // point the web test client at the Morp API in the container
        webTestClient = WebTestClient.bindToServer(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://localhost:" + getMorpContainer().getMappedPort(8081))
                .build();

        System.out.println("Keycloak: " + keycloakContainer.getAuthServerUrl());
        System.out.println("Morp server port: " + morpContainer.getMappedPort(8081));
        System.out.println("Morp actuator port: " + morpContainer.getMappedPort(8082));
    }

    @AfterEach
    final void afterBase() {
        mockServerClient.reset();
    }

    public static Network getNetwork() {
        return network;
    }

    public static KeycloakContainer getKeycloakContainer() {
        return keycloakContainer;
    }

    public static MockServerContainer getMockserverContainer() {
        return mockserverContainer;
    }

    public static GenericContainer getMorpContainer() {
        return morpContainer;
    }

    public static MockServerClient getMockServerClient() {
        return mockServerClient;
    }

    public static WebTestClient getWebTestClient() {
        return webTestClient;
    }
}
