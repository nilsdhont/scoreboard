package be.brigandze.control;

import static be.brigandze.control.EventController.getInstance;

import io.quarkus.scheduler.Scheduled;
import javax.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.Setter;

@ApplicationScoped
@Getter
public class ScoreBoardController {

    @Setter
    private int teamId;
    @Setter
    private int matchId;

    EventController eventController = getInstance();

    private Match currentMatch;

    @Scheduled(every = "10m")
    void updateCurrentMatch() {
        eventController.updateCurrentMatch();
    }

    @Scheduled(every = "10s")
    void updateScoreBoard() {
        this.currentMatch = eventController.getCurrentMatch();
        if (currentMatch != null) {
            currentMatch.updateScore();
        } else {
            eventController.updateCurrentMatch();
        }

    }


}
