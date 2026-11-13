package be.brigandze.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScoreBoardControllerTest {

    private static final long GIF_MS = 15_000;

    private final ScoreBoardController controller = new ScoreBoardController();
    private long now = 1_000_000;

    @BeforeEach
    void fixClock() {
        controller.clock = () -> now;
    }

    @Test
    void pointsGainedDecideTheGifs() {
        assertEquals(List.of(Gif.TRY), ScoreBoardController.gifsFor(5));
        assertEquals(List.of(Gif.TRY, Gif.CONVERSION), ScoreBoardController.gifsFor(7));
        assertEquals(List.of(Gif.CONVERSION), ScoreBoardController.gifsFor(2));
        assertEquals(List.of(Gif.PENALTY), ScoreBoardController.gifsFor(3));
        for (int points : new int[]{-5, 0, 1, 4, 6, 8, 10, 12, 14}) {
            assertEquals(List.of(), ScoreBoardController.gifsFor(points), points + " points");
        }
    }

    @Test
    void convertedTryShowsTheTryThenTheConversion() {
        controller.celebrate(7);

        assertEquals(Gif.TRY.getFile(), controller.getBoard().gif());
        now += GIF_MS - 1;
        assertEquals(Gif.TRY.getFile(), controller.getBoard().gif());
        now += 1;
        assertEquals(Gif.CONVERSION.getFile(), controller.getBoard().gif());
        now += GIF_MS;
        assertNull(controller.getBoard().gif());
    }

    @Test
    void scoreDuringAGifWaitsForItToFinish() {
        controller.celebrate(5);
        now += 10_000;
        controller.celebrate(3);

        assertEquals(Gif.TRY.getFile(), controller.getBoard().gif());
        now += 5_000;
        assertEquals(Gif.PENALTY.getFile(), controller.getBoard().gif());
        now += GIF_MS;
        assertNull(controller.getBoard().gif());
    }

    @Test
    void gifPickedOnThePhoneDropsWhatWasWaiting() {
        controller.celebrate(7);
        controller.gif(Gif.BASICS);

        assertEquals(Gif.BASICS.getFile(), controller.getBoard().gif());
        now += GIF_MS;
        assertNull(controller.getBoard().gif());
    }

    @Test
    void nothingIsShownWhileSwitchedOffOrInManualMode() {
        controller.setAutoGifs(false);
        controller.celebrate(5);
        assertNull(controller.getBoard().gif());

        controller.setAutoGifs(true);
        controller.setManual(true);
        controller.celebrate(5);
        assertNull(controller.getBoard().gif());

        controller.setManual(false);
        controller.celebrate(5);
        assertEquals(Gif.TRY.getFile(), controller.getBoard().gif());
    }
}
