package io.jaconi.morp;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.shaded.com.google.common.util.concurrent.Uninterruptibles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.fail;

/**
 * Usage:
 * <pre>
 * public class MyIntegrationTest {
 *
 *     &#64;RegisterExtension
 *     private static SeleniumExtension selenium = new SeleniumExtension();
 *
 *     ...
 * }
 * </pre>
 */
public class SeleniumExtension extends MORPExtension implements BeforeAllCallback {
    private static final BrowserWebDriverContainer<?> SELENIUM_CONTAINER;

    static {
        // Start Selenium with a Chrome browser.
        //noinspection resource
        SELENIUM_CONTAINER = new BrowserWebDriverContainer<>(
                ArmUtil.select("seleniarm/standalone-chromium", "selenium/standalone-chrome:103.0"))
                .withCapabilities(new ChromeOptions());
        //.withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, Path.of("./build").toFile());
        SELENIUM_CONTAINER.withNetwork(MORPExtension.NETWORK).start();
        System.out.println("VNC URL is: " + SELENIUM_CONTAINER.getVncAddress());
    }

    private RemoteWebDriver driver;

    void initDriver() {
        // get the web driver into the Selenium container
        driver = SELENIUM_CONTAINER.getWebDriver();
        // use implicit wait of 10s (for DOM to build) when asking for page elements
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    void resetDriver() {
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

    @Override
    public void afterEach(ExtensionContext context) {
        super.afterEach(context);
        resetDriver();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        initDriver();
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
