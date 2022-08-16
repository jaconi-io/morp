package io.jaconi.morp;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
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
 * Integration test extension, that sets up MORP, Keycloak and MockServer (as programmable backend) via TestContainers.
 * Integration tests running against MORP are expected to use this extension so that the container setup can be shared
 * across test executions. Container cleanup is done by TestContainers at the end of the JVM test run (by a cleanup
 * container).
 * <p>
 * Usage:
 * <pre>
 * &#64;ExtendWith(MORPExtension.class)
 * public class MyIntegrationTest {
 *     ...
 * }
 * </pre>
 */
public class MORPExtension implements AfterEachCallback {
    static final MockServerClient MOCK_SERVER_CLIENT;
    static final WebTestClient WEB_TEST_CLIENT;
    static final Network NETWORK;
    static final KeycloakContainer KEYCLOAK_CONTAINER;
    private static final MockServerContainer MOCK_SERVER_CONTAINER;
    private static final GenericContainer<?> MORP_CONTAINER;

    static {
        // We need a network for our containers to see each other.
        NETWORK = Network.newNetwork();

        // Start Keycloak in that network.
        KEYCLOAK_CONTAINER = new KeycloakContainer("quay.io/keycloak/keycloak:19.0.1")
                .withRealmImportFile("/keycloak/realm-1.json")
                .withNetwork(NETWORK)
                .withExposedPorts(8080)
                .withNetworkAliases("keycloak")
                .withEnv("KC_HOSTNAME_STRICT", "false")
                .withEnv("KC_HOSTNAME_STRICT_HTTPS", "false")
                .withEnv("KC_PROXY", "edge");
        KEYCLOAK_CONTAINER.start();

        // Start mockserver as generic upstream (protected by MORP).
        MOCK_SERVER_CONTAINER = new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"))
                .withNetwork(NETWORK)
                .withNetworkAliases("upstream");
        MOCK_SERVER_CONTAINER
                .withLogConsumer(new Slf4jLogConsumer(DockerLoggerFactory.getLogger(MOCK_SERVER_CONTAINER.getDockerImageName())))
                .start();

        // Point our mockserver client to mockserver running in the container.
        MOCK_SERVER_CLIENT = new MockServerClient(MOCK_SERVER_CONTAINER.getHost(), MOCK_SERVER_CONTAINER.getServerPort());

        // Start MORP (auth proxy) in that network.
        //noinspection resource
        MORP_CONTAINER = new GenericContainer<>(DockerImageName.parse("ghcr.io/jaconi-io/morp:latest"))
                .withNetwork(NETWORK)
                .withNetworkAliases("morp", "tenant1-morp")
                .withExposedPorts(8081, 8082)
                .withEnv("SPRING_PROFILES_ACTIVE", "test")
                .withFileSystemBind(
                        "./src/integrationTest/resources/application-test.yaml",
                        "/workspace/config/application-test.yaml",
                        BindMode.READ_ONLY)
                .waitingFor(new HttpWaitStrategy()
                        .forPort(8082)
                        .forPath("/actuator/health/readiness")
                        .withStartupTimeout(Duration.ofMinutes(5)));
        MORP_CONTAINER
                .withLogConsumer(new Slf4jLogConsumer(DockerLoggerFactory.getLogger(MORP_CONTAINER.getDockerImageName())))
                .start();

        // Control the HTTP client to enable wiretap logs.
        HttpClient httpClient = HttpClient.create()
                .wiretap(true) // hex dump wiretap
                .compress(true);

        // Point the web test client at the MORP API in the container.
        WEB_TEST_CLIENT = WebTestClient.bindToServer(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://localhost:" + MORP_CONTAINER.getMappedPort(8081))
                .build();

        System.out.println("Keycloak: " + KEYCLOAK_CONTAINER.getAuthServerUrl());
        System.out.println("MORP server port: " + MORP_CONTAINER.getMappedPort(8081));
        System.out.println("MORP actuator port: " + MORP_CONTAINER.getMappedPort(8082));
    }

    @Override
    public void afterEach(ExtensionContext context) {
        MOCK_SERVER_CLIENT.reset();
    }
}
