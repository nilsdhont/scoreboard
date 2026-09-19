package be.brigandze.control;

import static be.brigandze.sporteasy.SportEasyResource.getSportEasyInstance;

import be.brigandze.entity.Event;
import org.jboss.logging.Logger;

public class Match {

    private static final Logger LOG = Logger.getLogger(Match.class);

    private final int teamId;
    private final int id;

    private int scoreBrigandZe;
    private String nameBrigandZe;
    private int scoreVisitors;
    private String nameVisitors;

    public Match(int teamId, int id, String nameBrigandZe, String nameVisitors) {
        this.teamId = teamId;
        this.id = id;
        this.nameBrigandZe = nameBrigandZe;
        this.nameVisitors = nameVisitors;
    }

    public int getTeamId() {
        return teamId;
    }

    public int getId() {
        return id;
    }

    public int getScoreBrigandZe() {
        return scoreBrigandZe;
    }

    public String getNameBrigandZe() {
        return nameBrigandZe;
    }

    public int getScoreVisitors() {
        return scoreVisitors;
    }

    public String getNameVisitors() {
        return nameVisitors;
    }

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
        }

    }

    void printScore() {
        String score = nameBrigandZe +
            ": " +
            scoreBrigandZe +
            " - " +
            nameVisitors +
            ": " +
            scoreVisitors;
        LOG.info(score);
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
