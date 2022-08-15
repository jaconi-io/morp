package io.jaconi.morp;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.mockserver.client.MockServerClient;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.*;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.shaded.com.google.common.util.concurrent.Uninterruptibles;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.DockerLoggerFactory;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.fail;

public class TestSeleniumBase extends TestBase {

    private static BrowserWebDriverContainer seleniumContainer;

    private RemoteWebDriver driver;

    static {
        // start Selenium with a Chrome browser
        seleniumContainer = new BrowserWebDriverContainer(
                ArmUtil.select("seleniarm/standalone-chromium", "selenium/standalone-chrome:103.0"))
                .withCapabilities(new ChromeOptions());
        //.withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, Path.of("./build").toFile());
        seleniumContainer.withNetwork(getNetwork()).start();
        System.out.println("VNC URL is: " + seleniumContainer.getVncAddress());
    }

    public void resetDriver() {
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
        Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
    }

    public RemoteWebDriver getDriver() {
        return driver;
    }

    public void saveScreenshot(String name) {
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
