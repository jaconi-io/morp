package io.jaconi.morp;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * This integration tests checks whether a Morp protected backend (mockserver) can be accesses via various identity
 * providers (as defined in application-selenium.yaml)
 */
public class SeleniumIT {

    @RegisterExtension
    private static final SeleniumExtension SELENIUM = new SeleniumExtension();

    @BeforeEach
    void setUp() throws Exception {

        // Setup upstream behavior via mockserver client.
        MORPExtension.MOCK_SERVER_CLIENT
                .when(request()
                        .withMethod("GET")
                        .withPath("/test"))
                .respond(response()
                        .withContentType(MediaType.TEXT_HTML)
                        .withStatusCode(200)
                        .withBody("<h1 id='test'>Hello from mockserver</h1>"));

        SELENIUM.resetDriver();
    }

    @ParameterizedTest
    @CsvSource({
            "tenant1, morp:8081, /upstream/tenant1, /test",
            "tenant1, tenant1-morp:8081, /upstream, /test"
    })
    void testWithKeycloak(String tenant, String host, String prefix, String path) {
        RemoteWebDriver driver = SELENIUM.getDriver();

        // have browser access the protected upstream via Morp
        driver.get("http://" + host + prefix + path);

        // expect redirects to take us to the Keycloak login mast
        SELENIUM.saveScreenshot("01-keycloak-login-mask.png");
        driver.findElement(By.id("kc-form-login"));

        // fill the form with our admin credentials
        driver.findElement(By.id("username")).sendKeys("test@jaconi.io");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("kc-login")).click();

        // match upstream browser content
        SELENIUM.saveScreenshot("02-keycloak-after-login.png");
        assertThat(driver.findElement(By.id("test")).getText()).isEqualTo("Hello from mockserver");

        // assert what upstream has seen (i.e. headers etc)
        MORPExtension.MOCK_SERVER_CLIENT.verify(
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
}
