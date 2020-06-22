package be.brigandze.control;

import io.quarkus.scheduler.Scheduled;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScoreBoardController {

    private Match match;

    public void createMatch() {
        //TODO find the id on sporteasy from the match of the day. or ask it via input?
        //TODO and set a team for the BrigandZelles
        match = new Match(594671, 7718818);

    }


    @Scheduled(every = "10s")
    public void updateScoreBoard() {
        if (match == null) {
            createMatch();
        }

        if (match != null) {
            match.updateScore();
            printScore(match);
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


}
