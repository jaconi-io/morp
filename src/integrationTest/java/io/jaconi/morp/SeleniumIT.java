package io.jaconi.morp;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockserver.model.MediaType;
import org.mockserver.model.NottableString;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


/**
 * Integration test that runs a real web browser (i.e. Chrome via Selenium TestContainers) to verify successful
 * login flows with a number of OIDC identity providers.
 */
@Testcontainers
@SpringBootTest
public class SeleniumIT extends TestBase {

    @Configuration
    static class Config {
    }

    @Value("${test.okta.username}")
    private String oktaUsername;

    @Value("${test.okta.password}")
    private String oktaPassword;


    // we start Chrome for each test to ensure a clean state (i.e. cookies etc)
    // screen recording not working on ARM Mac due to missing ARM image of vnc-recorder
    @Container
    public BrowserWebDriverContainer chrome = new BrowserWebDriverContainer<>(ArmUtil.select("seleniarm/standalone-chromium:103.0", "selenium/standalone-chrome:103.0"))
            .withNetwork(containerSetup.getNetwork())
            .withNetworkAliases("chrome")
            .withCapabilities(new ChromeOptions())
            .withStartupTimeout(Duration.ofSeconds(30))
            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.SKIP, new File("./build/"))
            .withRecordingFileFactory(new TruncatedRecordingFileFactory());

    protected RemoteWebDriver driver;

    @BeforeEach
    void setUp() {
        // use implicit wait of 10s (for DOM to build) when asking for page elements
        driver = chrome.getWebDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Setup upstream behavior via mockserver client.
        containerSetup.getMockServerClient()
                .when(request()
                        .withMethod("GET")
                        .withPath("/test"))
                .respond(response()
                        .withContentType(MediaType.TEXT_HTML)
                        .withStatusCode(200)
                        .withBody("<h1 id='test'>Hello from mockserver</h1>"));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.close();
    }

    @ParameterizedTest
    @CsvSource({
            "tenant1, morp:8081, /upstream/tenant1, /test",
            "tenant1, tenant1-morp:8081, /upstream, /test",
            "tenant1a, morp:8081, /upstream/tenant1a, /test",
            "tenant1a, tenant1a-morp:8081, /upstream, /test"
    })
    void testWithKeycloak(String tenant, String host, String prefix, String path) throws MalformedURLException {

        // have browser access the protected upstream via Morp
        driver.get("http://" + host + prefix + path);

        // expect redirects to take us to the Keycloak login mast
        //chrome.saveScreenshot("01-keycloak-login-mask.png");
        driver.findElement(By.id("kc-form-login"));

        // fill the form with our test user credentials
        driver.findElement(By.id("username")).sendKeys("test@jaconi.io");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("kc-login")).click();

        // match upstream browser content
        //SELENIUM.saveScreenshot("02-keycloak-after-login.png");
        // assert that we ended up in the right place
        URL url = new URL(driver.getCurrentUrl());
        assertThat(url.getHost()).isEqualTo(StringUtils.substringBefore(host, ":"));
        assertThat(url.getPort()).isEqualTo(8081);
        assertThat(url.getPath()).isEqualTo(prefix + path);

        assertThat(driver.findElement(By.id("test")).getText()).isEqualTo("Hello from mockserver");

        // assert what upstream has seen (i.e. headers etc)
        containerSetup.getMockServerClient()
                .verify(request()
                        .withMethod("GET")
                        .withPath(path)
                        // expect host header rewritten by proxy
                        .withHeader("host", "upstream:1080")
                        // expect gateway proxy headers
                        .withHeader("x-forwarded-host", host)
                        .withHeader("x-forwarded-prefix", prefix)
                        // expect custom tenant header
                        .withHeader("x-tenant-id", tenant)
                        // expect security session cookie to be removed
                        .withCookie(NottableString.not("MORP_SESSION"), NottableString.string(".*")));
    }

    @ParameterizedTest
    @CsvSource({
            "tenant2, morp:8081, /upstream/tenant2, /test",
            "tenant2, tenant2-morp:8081, /upstream, /test"
    })
    void testWithOkta(String tenant, String host, String prefix, String path) {

        // have browser access the protected upstream via Morp
        driver.get("http://" + host + prefix + path);

        // expect redirects to take us to the Keycloak login mast
        //SELENIUM.saveScreenshot("01-okta-login-mask.png");
        driver.findElement(By.id("okta-sign-in"));

        // fill login form with our test user credentials
        driver.findElement(By.id("okta-signin-username")).sendKeys(oktaUsername);
        driver.findElement(By.id("okta-signin-password")).sendKeys(oktaPassword);
        //SELENIUM.saveScreenshot("02-okta-login-filled.png");
        driver.findElement(By.id("okta-signin-submit")).click();

        // match upstream browser content
        assertThat(driver.findElement(By.id("test")).getText()).isEqualTo("Hello from mockserver");
        //SELENIUM.saveScreenshot("03-okta-after-login.png");

        // assert what upstream has seen (i.e. headers etc)
        containerSetup.getMockServerClient()
                .verify(request()
                        .withMethod("GET")
                        .withPath(path)
                        // expect host header rewritten by proxy
                        .withHeader("host", "upstream:1080")
                        // expect gateway proxy headers
                        .withHeader("x-forwarded-host", host)
                        .withHeader("x-forwarded-prefix", prefix)
                        // expect custom tenant header
                        .withHeader("x-tenant-id", tenant)
                        // expect security session cookie to be removed
                        .withCookie(NottableString.not("MORP_SESSION"), NottableString.string(".*")));
    }
}
