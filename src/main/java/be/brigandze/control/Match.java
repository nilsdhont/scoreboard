package be.brigandze.control;

import be.brigandze.entity.MatchData;
import be.brigandze.rest.SportEasyResource;
import lombok.Getter;

@Getter
public class Match {

    boolean namesSet;
    private final SportEasyResource sportEasyResource;
    private int scoreBrigandZe = 0;
    private String nameBrigandZe;
    private int scoreVisitors = 0;
    private String nameVisitors;

    public Match(int teamId, int id) {
        this.sportEasyResource = new SportEasyResource(teamId, id);
    }

    public void updateScore() {
        MatchData matchData = sportEasyResource.getMatchData();

        if (matchData != null) {
            scoreBrigandZe = matchData.getOpponent_left().getScore();
            scoreVisitors = matchData.getOpponent_right().getScore();


            if (!namesSet) {
                nameBrigandZe = matchData.getOpponent_left().getFull_name();
                nameVisitors = matchData.getOpponent_right().getFull_name();
            }
        }

    }
}
