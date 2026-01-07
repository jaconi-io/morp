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
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.DockerLoggerFactory;
import reactor.netty.http.client.HttpClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * A Junit5 extensions representing our TestContainers based test setup that can be shared by multiple integration
 * test classes.
 */
public class TestContainerSetup implements AfterEachCallback {

    private final Network network;
    private final KeycloakContainer keycloak;

    private final MockServerContainer mockserver;

    private final GenericContainer morp;

    private final MockServerClient mockServerClient;

    private final WebTestClient webTestClient;

    private final WebTestClient managementTestClient;

    public TestContainerSetup() {
        this.network = Network.newNetwork();

        // setup Keycloak container
        this.keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.0")
                .withRealmImportFile("/keycloak/realm-1.json")
                .withNetwork(network)
                .withNetworkAliases("keycloak")
                .withEnv("KC_HOSTNAME_STRICT", "false")
                .withEnv("KC_HOSTNAME_STRICT_HTTPS", "false")
                .withEnv("KC_PROXY", "edge");

        // setup mockserver (as protected upstream)
        var tag = "mockserver-%s".formatted(MockServerClient.class.getPackage().getImplementationVersion());
        var mockServerImage = DockerImageName.parse("mockserver/mockserver").withTag(tag);
        this.mockserver = new MockServerContainer(mockServerImage)
                .withNetwork(network)
                .withNetworkAliases("upstream")
                .withLogConsumer(new Slf4jLogConsumer(DockerLoggerFactory.getLogger(mockServerImage.asCanonicalNameString())));

        // setup morp as auth proxy for upstream
        var morpImage = DockerImageName.parse("ghcr.io/jaconi-io/morp:latest");
        this.morp = new GenericContainer<>(morpImage)
                .withNetwork(network)
                .withNetworkAliases("morp", "tenant1-morp", "tenant2-morp")
                .withExposedPorts(8080, 8081)
				//.withEnv("JAVA_OPTS", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:6666")
                .withEnv("SPRING_PROFILES_ACTIVE", "test")
                .withFileSystemBind(
                        "./src/integrationTest/resources/morp/application.yaml",
                        "/workspace/config/application.yaml",
                        BindMode.READ_ONLY)
                .waitingFor(new HttpWaitStrategy()
                        .forPort(8081)
                        .forPath("/actuator/health/readiness")
                        .withStartupTimeout(Duration.ofMinutes(5)))
                .withLogConsumer(new Slf4jLogConsumer(DockerLoggerFactory.getLogger(morpImage.asCanonicalNameString())));

        // for local development convenience, bind mount the git-ignored 'secret.properties' (if it exists)
        if (Files.exists(Path.of("./secret.properties"))) {
            morp.withFileSystemBind(
                    "./secret.properties",
                    "/workspace/config/secret.properties",
                    BindMode.READ_ONLY);
        }

        // for morp pass any ENV variables with prefix "MORP_"
        // this is relevant for passing oauth client secrets from GitHub secrets all the way into TestContainers
        System.getenv().entrySet().stream()
                .filter(e -> e.getKey().startsWith("MORP_"))
                .forEach(e -> morp.withEnv(e.getKey(), e.getValue()));

        Startables.deepStart(keycloak, mockserver, morp).join();

        // create client to control mockserver (running as container)
        mockServerClient = new MockServerClient(mockserver.getHost(), mockserver.getServerPort());

        // Control the HTTP client to enable wiretap logs.
        var httpClient = HttpClient.create()
                .wiretap(true) // hex dump wiretap
                .compress(true);
        webTestClient = WebTestClient.bindToServer(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://localhost:" + morp.getMappedPort(8080))
                .build();

        managementTestClient = WebTestClient.bindToServer(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://localhost:" + morp.getMappedPort(8081))
                .build();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (mockServerClient != null) mockServerClient.reset();
    }

    public Network getNetwork() {
        return network;
    }

    public KeycloakContainer getKeycloak() {
        return keycloak;
    }

    public MockServerContainer getMockserver() {
        return mockserver;
    }

    public GenericContainer getMorp() {
        return morp;
    }

    public MockServerClient getMockServerClient() {
        return mockServerClient;
    }

    public WebTestClient getWebTestClient() {
        return webTestClient;
    }

    public WebTestClient getManagementTestClient() {
        return managementTestClient;
    }
}
