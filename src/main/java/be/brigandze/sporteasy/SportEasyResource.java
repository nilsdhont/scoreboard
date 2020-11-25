package be.brigandze.sporteasy;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.logging.Level.SEVERE;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.ACCEPTED;

import be.brigandze.entity.Event;
import be.brigandze.entity.TeamEventList;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;

public class SportEasyResource {

    private final static Logger LOGGER = Logger.getLogger(SportEasyResource.class.getName());

    private static SportEasyResource instance;

    String xCsrfToken;
    String cookie;
    final Client client = newClient();

    private boolean loggedIn = false;
    private boolean loggingIn = false;

    public static SportEasyResource getInstance() {
        if (instance == null) {
            instance = new SportEasyResource();
        }
        return instance;
    }

    private SportEasyResource() {
    }

    private boolean login() {
        if (!loggingIn) {
            loggingIn = true;
            SportEasyLogin sportEasyLogin = new SportEasyLogin();
            xCsrfToken = sportEasyLogin.getXCsrfToken();
            cookie = sportEasyLogin.getCookie();
            return true;
        }
        return false;
    }

    public TeamEventList getEvents(int teamId) {
        if (!loggedIn) {
            if (login()) {
                loggingIn = false;
                loggedIn = true;
            } else {
                return null;
            }
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
                LOGGER.log(SEVERE, "Error getting events from today", e);
            }
        } else {
            LOGGER.severe("Error getting event from SportEasy: " + response.getStatusInfo());
        }
        return null;
    }


    public Event getMatchData(int teamId, int eventId) {
        if (!loggedIn) {
            if (login()) {
                loggingIn = false;
                loggedIn = true;
            } else {
                return null;
            }
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
                LOGGER.log(SEVERE, "Error get match data. Team: " + teamId + ". Event: " + eventId,
                    e);
            }
        } else {
            LOGGER.severe("Error getting event from SportEasy: " + response.getStatusInfo());
        }
        return null;
    }

    public String getLiveStats(Event event) {
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
           LOGGER.severe("Error getting live info from SportEasy: " + response.getStatusInfo());
        }
        return "";
    }


    private void addLoginToHeader(Invocation.Builder request) {
        request.header("x-csrftoken", xCsrfToken);
        request.header("Cookie", cookie);
    }


}
