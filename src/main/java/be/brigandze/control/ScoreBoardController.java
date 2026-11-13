package be.brigandze.control;

import static be.brigandze.control.EventController.getInstance;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.LongSupplier;

@ApplicationScoped
public class ScoreBoardController {

    private static final long GIF_MS = 15_000;

    private final EventController eventController = getInstance();

    private volatile Match currentMatch;

    // manual mode: scores set from the phone replace the live ones until switched off again
    private boolean manual;
    private int manualHome;
    private int manualVisitors;
    // automatic gifs: celebrate what the home team scores in SportEasy, switchable from the phone
    private boolean autoGifs = true;
    private String gifFile;
    private long gifUntil;
    // gifs waiting for the one on the board to finish, so a try and its conversion play back to back
    private final Deque<String> gifQueue = new ArrayDeque<>();
    // replaced in tests
    LongSupplier clock = System::currentTimeMillis;
    // last uploaded test gif, kept in memory only
    private byte[] uploadedGif;
    private int uploadVersion;

    // gif is the url of the image to show, null while the score is on the board
    public record Board(String nameBrigandZe, String nameVisitors, int scoreBrigandZe, int scoreVisitors,
                        boolean manual, boolean autoGifs, String gif) {
    }

    public synchronized Board getBoard() {
        Match match = currentMatch;
        return new Board(
            match == null ? "BrigandZe" : match.getNameBrigandZe(),
            match == null ? "Visitors" : match.getNameVisitors(),
            manual ? manualHome : liveHome(),
            manual ? manualVisitors : liveVisitors(),
            manual,
            autoGifs,
            currentGif());
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

    public synchronized Board setAutoGifs(boolean on) {
        autoGifs = on;
        return getBoard();
    }

    // SportEasy only has the total, so what was scored is read from the points gained between two polls.
    // A try is entered together with its conversion, so that is 5 or 7 at once.
    static List<Gif> gifsFor(int points) {
        return switch (points) {
            case 5 -> List.of(Gif.TRY);
            case 7 -> List.of(Gif.TRY, Gif.CONVERSION);
            case 2 -> List.of(Gif.CONVERSION);
            case 3 -> List.of(Gif.PENALTY);
            // corrections, or several scores at once after SportEasy was unreachable: too late and too ambiguous
            default -> List.of();
        };
    }

    synchronized void celebrate(int points) {
        if (manual || !autoGifs) {
            return;
        }
        for (Gif gif : gifsFor(points)) {
            if (currentGif() == null) {
                gifFile = gif.getFile();
                gifUntil = clock.getAsLong() + GIF_MS;
            } else {
                gifQueue.add(gif.getFile());
            }
        }
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

    // a gif picked on the phone goes up right away and drops whatever was still waiting
    private Board showGif(String pFile) {
        gifQueue.clear();
        gifFile = pFile;
        gifUntil = clock.getAsLong() + GIF_MS;
        return getBoard();
    }

    private String currentGif() {
        long now = clock.getAsLong();
        // a waiting gif starts where the previous one ended
        while (now >= gifUntil && !gifQueue.isEmpty()) {
            gifFile = gifQueue.poll();
            gifUntil += GIF_MS;
        }
        return now < gifUntil ? gifFile : null;
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
            celebrate(currentMatch.updateScore());
        } else {
            eventController.updateCurrentMatch();
        }
    }
}
