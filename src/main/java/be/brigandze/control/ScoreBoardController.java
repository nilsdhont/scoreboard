package be.brigandze.control;

import static be.brigandze.control.EventController.getInstance;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ScoreBoardController {

    private static final long GIF_MS = 15_000;

    private final EventController eventController = getInstance();

    private volatile Match currentMatch;

    // manual mode: scores set from the phone replace the live ones until switched off again
    private boolean manual;
    private int manualHome;
    private int manualVisitors;
    private String gifFile;
    private long gifUntil;
    // last uploaded test gif, kept in memory only
    private byte[] uploadedGif;
    private int uploadVersion;

    // gif is the url of the image to show, null while the score is on the board
    public record Board(String nameBrigandZe, String nameVisitors, int scoreBrigandZe, int scoreVisitors,
                        boolean manual, String gif) {
    }

    public synchronized Board getBoard() {
        Match match = currentMatch;
        return new Board(
            match == null ? "BrigandZe" : match.getNameBrigandZe(),
            match == null ? "Visitors" : match.getNameVisitors(),
            manual ? manualHome : liveHome(),
            manual ? manualVisitors : liveVisitors(),
            manual,
            System.currentTimeMillis() < gifUntil ? gifFile : null);
    }

    // manual scores start from the live score so the phone only has to correct from there
    public synchronized Board setManual(boolean on) {
        if (on && !manual) {
            manualHome = liveHome();
            manualVisitors = liveVisitors();
        }
        manual = on;
        return getBoard();
    }

    public synchronized Board bump(int home, int visitors) {
        if (manual) {
            manualHome = Math.max(0, manualHome + home);
            manualVisitors = Math.max(0, manualVisitors + visitors);
        }
        return getBoard();
    }

    public synchronized Board gif(Gif pGif) {
        return showGif(pGif.getFile());
    }

    // the version in the url makes every upload a new address, so clients restart and never use a cached one
    public synchronized Board uploadGif(byte[] pGif) {
        uploadedGif = pGif;
        uploadVersion++;
        return showGif("matchData/gif/uploaded/" + uploadVersion);
    }

    public synchronized byte[] getUploadedGif() {
        return uploadedGif;
    }

    private Board showGif(String pFile) {
        gifFile = pFile;
        gifUntil = System.currentTimeMillis() + GIF_MS;
        return getBoard();
    }

    private int liveHome() {
        Match match = currentMatch;
        return match == null ? 0 : match.getScoreBrigandZe();
    }

    private int liveVisitors() {
        Match match = currentMatch;
        return match == null ? 0 : match.getScoreVisitors();
    }

    @Scheduled(every = "10m")
    void updateCurrentMatch() {
        eventController.updateCurrentMatch();
    }

    @Scheduled(every = "10s")
    void updateScoreBoard() {
        currentMatch = eventController.getCurrentMatch();
        if (currentMatch != null) {
            currentMatch.updateScore();
        } else {
            eventController.updateCurrentMatch();
        }
    }
}
