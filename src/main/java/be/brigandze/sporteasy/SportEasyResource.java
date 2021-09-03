package be.brigandze.sporteasy;

import static java.nio.charset.Charset.defaultCharset;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.ACCEPTED;

import be.brigandze.entity.Event;
import be.brigandze.entity.TeamEventList;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;

public class SportEasyResource {

    private static final Logger LOG = Logger.getLogger(SportEasyResource.class);

    private static SportEasyResource instance;
    final Client client;
    private boolean loggedIn = false;
    private boolean loggingIn = false;

    private String xCsrfToken;
    private String cookie;
    private LocalDate expirationDate;

    private SportEasyResource() {
        client = newClient();
    }

    public static SportEasyResource getSportEasyInstance() {
        if (instance == null) {
            instance = new SportEasyResource();
        }
        return instance;
    }

    private boolean login() {
        if (!loggingIn) {
            loggingIn = true;
            SportEasyLogin sportEasyLogin = new SportEasyLogin();
            sportEasyLogin.login();
            xCsrfToken = sportEasyLogin.getXCsrfToken();
            cookie = sportEasyLogin.getCookie();
            expirationDate = LocalDate.ofInstant(
                sportEasyLogin.getCookieExpirationDate().toInstant(),
                ZoneId.systemDefault());
            return true;
        }
        return false;
    }

    public TeamEventList getEvents(int teamId) {
        if (notLoggedIn()) {
            return null;
        }

        WebTarget eventsTarget = client
            .target("https://api.sporteasy.net/v2.1/teams/" + teamId + "/events/?around=TODAY");
        Invocation.Builder request = eventsTarget.request(APPLICATION_JSON_TYPE);
        addLoginToHeader(request);
        Response response = request.get();
        if (response.getStatus() != ACCEPTED.getStatusCode()) {
            try {
                String data = IOUtils
                    .toString((InputStream) response.getEntity(), defaultCharset());
                return JsonbBuilder.create().fromJson(data, TeamEventList.class);

            } catch (IOException e) {
                LOG.error("Error getting events from today", e);
            }
        } else {
            LOG.error("Error getting event from SportEasy: " + response.getStatusInfo());
        }
        return null;
    }


    public Event getMatchData(int teamId, int eventId) {
        if (notLoggedIn()) {
            return null;
        }

        WebTarget matchTarget = client
            .target("https://api.sporteasy.net/v2.1/teams/" + teamId + "/events/" + eventId + "/");
        Invocation.Builder request = matchTarget.request(APPLICATION_JSON_TYPE);
        addLoginToHeader(request);
        Response response = request.get();
        if (response.getStatus() != ACCEPTED.getStatusCode()) {
            try {
                String data = IOUtils
                    .toString((InputStream) response.getEntity(), defaultCharset());

                return JsonbBuilder.create().fromJson(data, Event.class);

            } catch (IOException e) {
                LOG.error("Error get match data. Team: " + teamId + ". Event: " + eventId, e);
            }
        } else {
            LOG.error("Error getting event from SportEasy: " + response.getStatusInfo());
        }
        return null;
    }

    public String getLiveStats(Event event) {
        if (notLoggedIn()) {
            return null;
        }

        Invocation.Builder request = client.target(event.get_links().getRead_live_stats().getUrl())
            .request(APPLICATION_JSON_TYPE);
        addLoginToHeader(request);
        Response response = request.get();
        if (response.getStatus() != ACCEPTED.getStatusCode()) {
            try {
                String data = IOUtils
                    .toString((InputStream) response.getEntity(), defaultCharset());
                //TODO To object
                return data;

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            LOG.error("Error getting live info from SportEasy: " + response.getStatusInfo());
        }
        return "";
    }


    private boolean notLoggedIn() {
        if (!loggedIn) {
            if (login()) {
                loggingIn = false;
                loggedIn = true;
            } else {
                return true;
            }
        }

        if (cookieIsExpired()) {
            login();
        }

        return false;
    }

    public boolean cookieIsExpired() {
        return !LocalDate.now().atStartOfDay().isBefore(expirationDate.atStartOfDay());
    }

    private void addLoginToHeader(Invocation.Builder request) {
        request.header("x-csrftoken", xCsrfToken);
        request.header("Cookie", cookie);
    }

}
