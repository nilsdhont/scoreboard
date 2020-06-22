package be.brigandze.rest;

import be.brigandze.entity.MatchData;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.Charset.defaultCharset;
import static javax.ws.rs.client.ClientBuilder.newClient;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.ACCEPTED;

public class SportEasyResource {

    private final WebTarget matchTarget;
    private final String xCsrfToken;
    private final String cookie;
    private final Client client = newClient();

    @Inject
    public SportEasyResource(int teamId, int matchId) {
        SportEasyConfig sportEasyConfig = new SportEasyConfig();
        xCsrfToken = sportEasyConfig.getValue("x-csrftoken");
        cookie = sportEasyConfig.getValue("cookie");
        matchTarget = client.target("https://api.sporteasy.net/v2.1/teams/" + teamId + "/events/" + matchId + "/");
    }


    public MatchData getMatchData() {
        Invocation.Builder request = matchTarget.request(APPLICATION_JSON_TYPE);
        addLoginToHeader(request);
        Response response = request.get();
        if (response.getStatus() != ACCEPTED.getStatusCode()) {
            try {
                String data = IOUtils.toString((InputStream) response.getEntity(), defaultCharset());

                return JsonbBuilder.create().fromJson(data, MatchData.class);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error getting info from SportEasy: " + response.getStatusInfo());
        }
        return null;
    }

    public String getLiveStats(MatchData matchData) {
        Invocation.Builder request = client.target(matchData.get_links().getRead_live_stats().getUrl()).request(APPLICATION_JSON_TYPE);
        addLoginToHeader(request);
        Response response = request.get();
        if (response.getStatus() != ACCEPTED.getStatusCode()) {
            try {
                String data = IOUtils.toString((InputStream) response.getEntity(), defaultCharset());
                //TODO To object
                return data;

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error getting live info from SportEasy: " + response.getStatusInfo());
        }
        return "";
    }


    private void addLoginToHeader(Invocation.Builder request) {
        request.header("x-csrftoken", xCsrfToken);
        request.header("Cookie", cookie);
    }


}
