package io.jaconi.morp;


import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.*;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.DockerLoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

// TODO run morp as container inside TestContainer setup
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles({"test", "wiretap"})
//@TestPropertySource(properties = "keycloak.host=host.docker.internal")
public class SeleniumIT {

    private static Logger log = LoggerFactory.getLogger(SeleniumIT.class);

    private static BrowserWebDriverContainer seleniumContainer;
    private static KeycloakContainer keycloakContainer;
    private static MockServerContainer mockserverContainer;
    private static GenericContainer morpContainer;

    private MockServerClient mockServerClient;
    private RemoteWebDriver driver;

    @BeforeAll
    // TODO run Keycloak as container in the same network
    // TODO run Morp as container in the same network
    static void setup() {

        // we need a network for our containers to see each other
        Network network = Network.newNetwork();


        // start Selenium in that network
        seleniumContainer = new BrowserWebDriverContainer(
                ArmUtil.select("seleniarm/standalone-chromium", "selenium/standalone-chrome:latest"))
                .withCapabilities(new ChromeOptions());
        //.withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, Path.of("./build").toFile());
        seleniumContainer.withNetwork(network).start();

        // start Keycloak in that network
        keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:18.0.2")
                .withRealmImportFile("/keycloak/realm-1.json")
                .withNetwork(network)
                .withNetworkAliases("keycloak");
        keycloakContainer.start();

        // start mockserver as generic upstream (protected by Morp)
        mockserverContainer = new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.13.2"))
                .withNetwork(network)
                .withNetworkAliases("upstream");
        mockserverContainer
                .withLogConsumer(new Slf4jLogConsumer(DockerLoggerFactory.getLogger(mockserverContainer.getDockerImageName())))
                .start();

        // start Morp (auth proxy) in that network
        morpContainer = new GenericContainer(DockerImageName.parse("ghcr.io/jaconi-io/morp:latest"))
                .withNetwork(network)
                // TODO consider multiple alias to demonstrate tenant extraction from host
                .withNetworkAliases("morp")
                .withExposedPorts(8081, 8082)
                .withEnv("SPRING_PROFILES_ACTIVE", "test")
                .withFileSystemBind(
                        "./src/integrationTest/resources/application-selenium.yaml",
                        "/workspace/config/application-test.yaml",
                        BindMode.READ_ONLY)
                .waitingFor(new HttpWaitStrategy()
                        .forPort(8082)
                        .forPath("/actuator/health/readiness")
                        .withStartupTimeout(Duration.ofMinutes(5)));
        morpContainer
                .withLogConsumer(new Slf4jLogConsumer(DockerLoggerFactory.getLogger(morpContainer.getDockerImageName())))
                .start();

        System.out.println("Selenium remote URL is: " + seleniumContainer.getSeleniumAddress());
        System.out.println("VNC URL is: " + seleniumContainer.getVncAddress());
        System.out.println("Keycloak: " + keycloakContainer.getAuthServerUrl());
        System.out.println("Morp server port: " + morpContainer.getMappedPort(8081));
        System.out.println("Morp actuator port: " + morpContainer.getMappedPort(8082));
    }

    @BeforeEach
    void setUp() {

        // point our mockserver client to mockserver running in the container
        mockServerClient = new MockServerClient(mockserverContainer.getHost(), mockserverContainer.getServerPort());
        mockServerClient.reset();

        // get the web driver into the Selenium container
        driver = seleniumContainer.getWebDriver();
        // use implicit wait of 10s (for DOM to build) when asking for page elements
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Test
    // TODO consider making this a parameterized test (passing in provider + tenant to test)
    void testHappyPath() {

        // setup upstream behavior via mockserver client
        mockServerClient
                .when(request()
                        .withMethod("GET")
                        .withPath("/test"))
                .respond(response()
                        .withContentType(MediaType.TEXT_HTML)
                        .withStatusCode(200)
                        .withBody("<h1 id='test'>Hello from mockserver</h1>"));

        // have browser access the protected upstream via Morp
        driver.get("http://morp:8081/upstream/tenant1/test");

        // expect redirects to take us to the Keycloak login mast
        saveScreenshot("01-keycloak-login-mask.png");
        driver.findElement(By.id("kc-form-login"));

        // fill the form with our admin credentials
        driver.findElement(By.id("username")).sendKeys("test@jaconi.io");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("kc-login")).click();

        // match upstream browser content
        saveScreenshot("02-keycloak-after-login.png");
        assertThat(driver.findElement(By.id("test")).getText()).isEqualTo("Hello from mockserver");

        // assert what upstream has seen (i.e. headers etc)
        mockServerClient.verify(
                request()
                        .withMethod("GET")
                        .withPath("/test")
                        .withHeader("host", "upstream:1080")
                        // expect gateway proxy headers
                        .withHeader("x-forwarded-host", "morp:8081")
                        .withHeader("x-forwarded-prefix", "/upstream/tenant1")
                        .withHeader("x-forwarded-proto", "http")
                        // expect security session cookie (really?)
                        .withCookie("SESSION", ".+"), VerificationTimes.once());
    }

    // TODO move into a SeleniumUtil class so we can share it
    private void saveScreenshot(String name) {
        try {
            var screenshot = driver.getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot.toPath(),
                    Path.of("./build", Optional.ofNullable(name).orElse(screenshot.getName())),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            fail("Error taking screenshot", e);
        }
    }
}
