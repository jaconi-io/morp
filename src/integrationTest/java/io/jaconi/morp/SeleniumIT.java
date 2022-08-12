package io.jaconi.morp;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;

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

/**
 * This integration tests checks whether a Morp protected backend (mockserver) can be accesses via various identity
 * providers (as defined in application-selenium.yaml)
 */
public class SeleniumIT extends TestBase {

    private static BrowserWebDriverContainer seleniumContainer;
    private RemoteWebDriver driver;

    @BeforeAll
    static void setup() {
        // start Selenium with a Chrome browser
        seleniumContainer = new BrowserWebDriverContainer(
                ArmUtil.select("seleniarm/standalone-chromium", "selenium/standalone-chrome:latest"))
                .withCapabilities(new ChromeOptions());
        //.withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, Path.of("./build").toFile());
        seleniumContainer.withNetwork(getNetwork()).start();
        System.out.println("VNC URL is: " + seleniumContainer.getVncAddress());
    }

    @BeforeEach
    void setUp() throws Exception{

        // setup upstream behavior via mockserver client
        getMockServerClient()
                .when(request()
                        .withMethod("GET")
                        .withPath("/test"))
                .respond(response()
                        .withContentType(MediaType.TEXT_HTML)
                        .withStatusCode(200)
                        .withBody("<h1 id='test'>Hello from mockserver</h1>"));

        // get the web driver into the Selenium container
        driver = seleniumContainer.getWebDriver();
        // use implicit wait of 10s (for DOM to build) when asking for page elements
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // TODO consider jumping to IPD logout URL instead
        // hacky way to delete all cookies (cross domain) in Chrome
        driver.manage().deleteAllCookies();
        driver.get("chrome://settings/clearBrowserData");
        driver.findElement(By.xpath("//settings-ui")).sendKeys(
                Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.TAB, Keys.ENTER);
        Thread.sleep(5000);
    }

    @ParameterizedTest
    @CsvSource({
            "tenant1, morp:8081, /upstream/tenant1, /test",
            "tenant1, tenant1-morp:8081, /upstream, /test"
    })
    void testWithKeycloak(String tenant, String host, String prefix, String path) {

        // have browser access the protected upstream via Morp
        driver.get("http://" + host + prefix + path);

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
        getMockServerClient().verify(
                request()
                        .withMethod("GET")
                        .withPath(path)
                        // expect host header rewritten by proxy
                        .withHeader("host", "upstream:1080")
                        // expect gateway proxy headers
                        .withHeader("x-forwarded-host", host)
                        .withHeader("x-forwarded-prefix", prefix)
                        // expect custom tenant header
                        .withHeader("x-tenant-id", tenant)
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
