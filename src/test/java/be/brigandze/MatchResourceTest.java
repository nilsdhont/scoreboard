package be.brigandze;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class MatchResourceTest {

    @Test
    public void testMatchDataEndpoint() {
        given()
            .when().get("/matchdata")
            .then()
            .statusCode(200);
    }

}
