package be.brigandze.control;

import io.quarkus.scheduler.Scheduled;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static be.brigandze.control.EventController.getInstance;

@ApplicationScoped
@Getter
public class ScoreBoardController implements OperatingSystemDependent {

    @Setter
    private int teamId;
    @Setter
    private int matchId;

    @ConfigProperty(name = "scoreboard.config.path.pi")
    String scoreboardConfigFilePathPi;

    @ConfigProperty(name = "scoreboard.config.path.windows")
    String scoreboardConfigFilePathWindows;

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
            printScore(currentMatch);
        } else {
            eventController.updateCurrentMatch();
        }

    }

    private void printScore(Match match) {
        String s = match.getNameBrigandZe() +
                ": " +
                match.getScoreBrigandZe() +
                " - " +
                match.getNameVisitors() +
                ": " +
                match.getScoreVisitors();
        System.out.println(s);

    }

    private void readTeamAndMatchIdFromFile() {
        try {
            final Properties props = new Properties();
            props.load(new FileInputStream(getScoreboardConfigFilePath()));

            teamId = props.getProperty("team").equalsIgnoreCase("Brigandze") ? 594671 : 605596;
            matchId = Integer.valueOf(props.getProperty("match.id"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getScoreboardConfigFilePath() {
        return isWindows() ? scoreboardConfigFilePathWindows : scoreboardConfigFilePathPi;
    }


}
