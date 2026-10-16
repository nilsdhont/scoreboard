package be.brigandze.rest;

import be.brigandze.control.ScoreBoardController;
import be.brigandze.control.ScoreBoardController.Board;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * Board state for every client; the POST actions are what the phone taps.
 */
@Path("/matchData")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class MatchResource {

    @Inject
    ScoreBoardController scoreBoardController;

    @GET
    public Board get() {
        return scoreBoardController.getBoard();
    }

    @POST
    @Path("/manual")
    public Board manual(@QueryParam("on") boolean on) {
        return scoreBoardController.setManual(on);
    }

    @POST
    @Path("/score")
    public Board score(@QueryParam("home") @DefaultValue("0") int home,
                       @QueryParam("visitors") @DefaultValue("0") int visitors) {
        return scoreBoardController.bump(home, visitors);
    }

    @POST
    @Path("/gif")
    public Board gif() {
        return scoreBoardController.gif();
    }
}
