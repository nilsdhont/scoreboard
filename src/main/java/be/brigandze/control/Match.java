package be.brigandze.control;

import be.brigandze.entity.Event;
import be.brigandze.sporteasy.SportEasyResource;
import lombok.Getter;

import static be.brigandze.sporteasy.SportEasyResource.getInstance;

@Getter
public class Match {

    SportEasyResource sportEasyResource = getInstance();

    private final int teamId;
    private final int id;

    boolean namesSet;
    private int scoreBrigandZe = 0;
    private String nameBrigandZe;
    private int scoreVisitors = 0;
    private String nameVisitors;

    public Match(int teamId, int id) {
        this.teamId = teamId;
        this.id = id;
    }

    public void updateScore() {
        Event event = sportEasyResource.getMatchData(teamId, id);

        if (event != null) {
            scoreBrigandZe = event.getOpponent_left().getScore();
            scoreVisitors = event.getOpponent_right().getScore();


            if (!namesSet) {
                nameBrigandZe = event.getOpponent_left().getFull_name();
                nameVisitors = event.getOpponent_right().getFull_name();
            }
//            printLiveStats(matchData);
        }

    }

    public void printLiveStats(Event event) {
        String liveStats = sportEasyResource.getLiveStats(event);
        System.out.println(liveStats);
    }
}
