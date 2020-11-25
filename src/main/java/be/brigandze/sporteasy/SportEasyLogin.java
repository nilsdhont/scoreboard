package be.brigandze.sporteasy;

import static java.util.logging.Logger.getLogger;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SportEasyLogin {

    private final static Logger LOGGER = Logger.getLogger(SportEasyLogin.class.getName());

    private Cookie se_csrftoken;
    private Cookie sporteasy;

    public SportEasyLogin() {
        SportEasyConfig sportEasyConfig = new SportEasyConfig();
        getLogger("com.gargoylesoftware").setLevel(Level.SEVERE);
        try (final WebClient webClient = new WebClient()) {
            final HtmlPage page = webClient.getPage(
                "https://www.sporteasy.net/nl/login/?next=https://www.sporteasy.net/nl/profile/");

            HtmlForm login_form = (HtmlForm) page.getElementById("login_form");
            login_form.getInputByName("username").type(sportEasyConfig.getUsername());
            login_form.getInputByName("password").type(sportEasyConfig.getPassword());

            ((HtmlButton) page
                .getByXPath("//button[contains(@class, 'form__btn-validate')]").get(0)).click();

            se_csrftoken = webClient.getCookieManager().getCookie("se_csrftoken");
            sporteasy = webClient.getCookieManager().getCookie("sporteasy");


        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error logging in to sporteasy", e);
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

}
