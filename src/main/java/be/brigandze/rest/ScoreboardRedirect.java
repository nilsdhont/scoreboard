package be.brigandze.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.net.URI;

/**
 * The scoreboard used to live on the Angular route /scoreboard; keep that URL working.
 */
@Path("/scoreboard")
public class ScoreboardRedirect {

    @GET
    public Response redirectToRoot() {
        return Response.status(Response.Status.MOVED_PERMANENTLY).location(URI.create("/")).build();
    }
}
