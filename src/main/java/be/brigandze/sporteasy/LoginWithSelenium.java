package be.brigandze.sporteasy;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;

import static java.lang.System.getProperty;

public class LoginWithSelenium {


    private Cookie se_csrftoken;
    private Cookie sporteasy;


    public LoginWithSelenium() {
        try {
            SportEasyConfig sportEasyConfig = new SportEasyConfig();

            URL firefoxURL = getClass().getClassLoader().getResource("driver" + File.separator + getGeckoDriver());
            System.setProperty("webdriver.gecko.driver", Paths.get(firefoxURL.toURI()).toFile().getAbsolutePath());

            WebDriver driver = new FirefoxDriver();
            driver.get("https://www.sporteasy.net/nl/login/?next=https://www.sporteasy.net/nl/profile/");

            driver.findElement(By.id("id_username")).sendKeys(sportEasyConfig.getUsername());
            driver.findElement(By.id("id_password")).sendKeys(sportEasyConfig.getPassword());

            WebElement confirmBtn = driver.findElement(By.className("form__btn-validate"));
            confirmBtn.click();

            Set<Cookie> cookies = driver.manage().getCookies();
            se_csrftoken = cookies.stream().filter(s -> s.getName().equals("se_csrftoken")).findFirst().orElseThrow();
            sporteasy = cookies.stream().filter(s -> s.getName().equals("sporteasy")).findFirst().orElseThrow();

            driver.close();

        } catch (URISyntaxException e1) {
            e1.printStackTrace();
            System.out.println("cannot find driver file");
        }

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
        if (getProperty("os.name").contains("Windows")) {
            return "geckodriver-64-windows.exe";
        } else {
            return "geckodriver-64-linux.exe";
        }
    }
}
