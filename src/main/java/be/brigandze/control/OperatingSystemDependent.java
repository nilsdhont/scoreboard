package be.brigandze.control;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;

import static java.lang.System.getProperty;

public interface OperatingSystemDependent {

    default boolean isWindows() {
        return getProperty("os.name").toLowerCase().contains("windows");
    }

    default WebDriver getWebDriver() {
        return isWindows() ? getWindowsDriver() : getLinuxDriver();
    }

    private WebDriver getLinuxDriver() {
        System.setProperty("webdriver.chrome.driver", "/home/pi/Documents/scoreboard/geckodriver");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.setHeadless(true);
        chromeOptions.setLogLevel(ChromeDriverLogLevel.SEVERE);
        return new ChromeDriver(chromeOptions);
    }

    private WebDriver getWindowsDriver() {
        System.setProperty("webdriver.gecko.driver", "c:/temp/geckodriver-64-windows.exe");
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setHeadless(true);
        firefoxOptions.setLogLevel(FirefoxDriverLogLevel.ERROR);
        return new FirefoxDriver(firefoxOptions);
    }

}
