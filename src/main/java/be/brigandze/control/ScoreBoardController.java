package be.brigandze.control;

import static be.brigandze.control.EventController.getInstance;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScoreBoardController {

    private EventController eventController = getInstance();

    private Match currentMatch;

    public Match getCurrentMatch() {
        return currentMatch;
    }

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
