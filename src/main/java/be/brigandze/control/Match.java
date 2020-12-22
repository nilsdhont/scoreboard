package be.brigandze.control;

import static be.brigandze.sporteasy.SportEasyResource.*;

import be.brigandze.entity.Event;
import be.brigandze.sporteasy.SportEasyResource;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Match {

    private final int teamId;
    private final int id;

    private int scoreBrigandZe;
    private String nameBrigandZe;
    private int scoreVisitors;
    private String nameVisitors;
//
//    public Match(int teamId, int id) {
//        this.teamId = teamId;
//        this.id = id;
//    }

    public void updateScore() {
        Event event = getSportEasyInstance().getMatchData(teamId, id);

        if (event != null) {
            boolean scoreChanged = false;
            int newScoreBrigandZe = event.getOpponent_left().getScore();
            if (newScoreBrigandZe != scoreBrigandZe) {
                scoreChanged = true;
                scoreBrigandZe = newScoreBrigandZe;
            }
            int newScoreVisitors = event.getOpponent_right().getScore();
            if (newScoreVisitors != scoreVisitors) {
                scoreChanged = true;
                scoreVisitors = newScoreVisitors;
            }

            if (scoreChanged) {
                printScore();
            }
//            printLiveStats(matchData);
        }

    }

    void printScore() {
        String s = nameBrigandZe +
            ": " +
            scoreBrigandZe +
            " - " +
            nameVisitors +
            ": " +
            scoreVisitors;
        System.out.println(s);
    }

    public void printLiveStats(Event event) {
        String liveStats = getSportEasyInstance().getLiveStats(event);
        System.out.println(liveStats);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Match match = (Match) o;

        if (teamId != match.teamId) {
            return false;
        }
        return id == match.id;
    }

    @Override
    public int hashCode() {
        int result = teamId;
        result = 31 * result + id;
        return result;
    }
}
