package be.brigandze.sporteasy;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.Set;

import static java.lang.System.getProperty;

public class LoginWithSelenium {


    private Cookie se_csrftoken;
    private Cookie sporteasy;


    public LoginWithSelenium() {
        System.setProperty("webdriver.gecko.driver", getGeckoDriver());

        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setHeadless(true);
        firefoxOptions.setLogLevel(FirefoxDriverLogLevel.ERROR);
        WebDriver driver = new FirefoxDriver(firefoxOptions);
        driver.get("https://www.sporteasy.net/nl/login/?next=https://www.sporteasy.net/nl/profile/");


        SportEasyConfig sportEasyConfig = new SportEasyConfig();
        driver.findElement(By.id("id_username")).sendKeys(sportEasyConfig.getUsername());
        driver.findElement(By.id("id_password")).sendKeys(sportEasyConfig.getPassword());

        WebElement confirmBtn = driver.findElement(By.className("form__btn-validate"));
        confirmBtn.click();

        Set<Cookie> cookies = driver.manage().getCookies();
        se_csrftoken = cookies.stream().filter(s -> s.getName().equals("se_csrftoken")).findFirst().orElseThrow();
        sporteasy = cookies.stream().filter(s -> s.getName().equals("sporteasy")).findFirst().orElseThrow();

        driver.close();

    }

    public String getXCsrfToken() {
        return se_csrftoken.getValue();
    }

    public String getCookie() {
        return se_csrftoken.getName()
                + "="
                + se_csrftoken.getValue()
                + "; "
                + sporteasy.getName()
                + "= "
                + sporteasy.getValue();
    }

    private String getGeckoDriver() {
        if (getProperty("os.name").toLowerCase().contains("windows")) {
            return "c:/temp/geckodriver-64-windows.exe";
        } else {
            return "/tmp/geckodriver-64-linux.sh";
        }
    }
}
