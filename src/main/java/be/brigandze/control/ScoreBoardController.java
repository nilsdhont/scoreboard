package be.brigandze.control;

import io.quarkus.scheduler.Scheduled;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@ApplicationScoped
@Getter
public class ScoreBoardController {

    private Match match;

    @Setter
    private int teamId;
    @Setter
    private int matchId;

    @ConfigProperty(name = "scoreboard.config.path")
    private String scoreboardConfigFilePath;

    public void createMatch() {
        readTeamAndMatchIdFromFile();
        if (teamId > 0 && matchId > 0) {
            match = new Match(teamId, matchId);
        }

    }


    @Scheduled(every = "10s")
    public void updateScoreBoard() {
        if (match == null) {
            createMatch();
        }

        if (match != null) {
            match.updateScore();
//            printScore(match);
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
            props.load(new FileInputStream(scoreboardConfigFilePath));

            teamId = props.getProperty("team").equalsIgnoreCase("Brigandze") ? 594671 : 605596;
            matchId = Integer.valueOf(props.getProperty("match.id"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


}
