package be.brigandze.rest;

import be.brigandze.entity.MatchData;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.Charset.defaultCharset;

public class SportEasyResource {

    private Client client = ClientBuilder.newClient();
    private WebTarget matchTarget;
    private String xCsrfToken;
    private String cookie;

    @Inject
    public SportEasyResource(int teamId, int matchId) {
        SportEasyConfig sportEasyConfig = new SportEasyConfig();
        xCsrfToken = sportEasyConfig.getValue("x-csrftoken");
        cookie = sportEasyConfig.getValue("cookie");
        matchTarget = client.target("https://api.sporteasy.net/v2.1/teams/" + teamId + "/events/" + matchId + "/");
    }


    public MatchData getMatchData() {
        Invocation.Builder request = matchTarget.request();
        addLoginToHeader(request);
        Response response = request.get();
        if (response.getStatus() != Response.Status.ACCEPTED.getStatusCode()) {
            try {
                String data = IOUtils.toString((InputStream) response.getEntity(), defaultCharset());
                MatchData matchData = JsonbBuilder.create().fromJson(data, MatchData.class);

                return matchData;

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error getting info from SportEasy: " + response.getStatusInfo());
        }
        return null;
    }


    private void addLoginToHeader(Invocation.Builder request) {
        request.header("x-csrftoken", xCsrfToken);
        request.header("Cookie", cookie);
    }


}
