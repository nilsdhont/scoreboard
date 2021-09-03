package be.brigandze.rest;


import be.brigandze.control.Match;
import be.brigandze.control.ScoreBoardController;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
