package be.brigandze.sporteasy;

import be.brigandze.entity.Event;
import be.brigandze.entity.LoginData;
import be.brigandze.entity.TeamEventList;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jakarta.ws.rs.client.ClientBuilder.newClient;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.Response.Status.ACCEPTED;

public class SportEasyResource {

    private static final Logger LOG = Logger.getLogger(SportEasyResource.class);
    private static final Pattern THROTTLE_SECONDS = Pattern.compile("available in (\\d+) second");

    // lazy: newClient() starts Vert.x event-loop threads, which must not run during
    // native-image build-time class initialization
    private static SportEasyResource instance;
    final Client client;
    private boolean loggedIn = false;

    private String xCsrfToken;
    private String cookie;
    private LocalDate expirationDate;

    private SportEasyResource() {
        client = newClient();
    }

    public static synchronized SportEasyResource getSportEasyInstance() {
        if (instance == null) {
            instance = new SportEasyResource();
        }
        return instance;
    }

    private long nextLoginAllowed;

    private synchronized boolean login() {
        // SportEasy throttles authenticate with escalating 429 windows: never attempt while a
        // server-given wait is pending, and keep 5s between own attempts (startup fires 2 passes)
        if (System.currentTimeMillis() < nextLoginAllowed) {
            return false;
        }
        nextLoginAllowed = System.currentTimeMillis() + 5000;
        try {
            // sporteasy.username/password come from env vars (SPORTEASY_USERNAME/SPORTEASY_PASSWORD,
            // e.g. docker --env-file) or a .env file in the working directory
            Config config = ConfigProvider.getConfig();
            LoginData loginData = new LoginData();
            loginData.setUsername(config.getValue("sporteasy.username", String.class));
            loginData.setPassword(config.getValue("sporteasy.password", String.class));
            WebTarget loginTarget = client
                    .target("https://api.sporteasy.net/v2.1/account/authenticate/");
            Response response = loginTarget
                    .request(APPLICATION_JSON_TYPE)
                    .accept(APPLICATION_JSON_TYPE)
                    .buildPost(Entity.entity(loginData, APPLICATION_JSON_TYPE))
                    .invoke();
            List<Object> cookiesMetadata = response.getMetadata().get("Set-Cookie");
            if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)
                    || cookiesMetadata == null || cookiesMetadata.size() < 2) {
                String body = response.readEntity(String.class);
                long waitMillis = throttleMillis(response, body);
                nextLoginAllowed = System.currentTimeMillis() + waitMillis;
                LOG.error("Login sporteasy failed. Status: " + response.getStatus() + ". Body: " + body
                        + ". Next attempt in " + waitMillis / 1000 + "s");
                return false;
            }
            xCsrfToken = String.valueOf(cookiesMetadata.get(0));
            cookie = String.valueOf(cookiesMetadata.get(1));
            expirationDate = LocalDate.now().plusDays(10);
            return true;

        } catch (Exception e) {
            LOG.error("Error authentication sporteasy", e);
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
                return JsonbBuilder.create().fromJson(response.readEntity(String.class), TeamEventList.class);

            } catch (Exception e) {
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
        if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            String data = null;
            try {
                data = response.readEntity(String.class);

                return JsonbBuilder.create().fromJson(data, Event.class);

            } catch (Exception e) {
                if (data == null) {
                    LOG.error("Error get match data. Team: " + teamId + ". Event: " + eventId, e);
                } else {
                    LOG.error("Error parsing DATA: " + data, e);
                }
            }
        } else {
            LOG.error("Error getting event from SportEasy: " + response.getStatusInfo());
        }
        return null;
    }

    // "Retry-After" header, else SportEasy's "Expected available in N seconds" body, else 1 minute
    private long throttleMillis(Response response, String body) {
        String retryAfter = response.getHeaderString("Retry-After");
        if (retryAfter != null && retryAfter.matches("\\d+")) {
            return (Long.parseLong(retryAfter) + 2) * 1000;
        }
        Matcher matcher = THROTTLE_SECONDS.matcher(body == null ? "" : body);
        if (matcher.find()) {
            return (Long.parseLong(matcher.group(1)) + 2) * 1000;
        }
        return 60_000;
    }

    private boolean notLoggedIn() {
        if (!loggedIn) {
            if (login()) {
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
