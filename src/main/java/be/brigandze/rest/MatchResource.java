package be.brigandze.rest;


import be.brigandze.control.Match;
import be.brigandze.control.ScoreBoardController;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/matchData")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class MatchResource {

    @Inject
    ScoreBoardController scoreBoardController;

    @GET
    public Match get() {
        return scoreBoardController.getCurrentMatch();
    }
}
