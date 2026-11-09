package be.brigandze.control;

/**
 * Celebration gifs the board can show instead of the score.
 */
public enum Gif {
    TRY("brigandze_try_fireworks_192x64.gif"),
    CONVERSION("brigandze_conversion_192x64.gif"),
    PENALTY("brigandze_penalty_192x64.gif"),
    BASICS("brigandze_basics_192x64.gif");

    private final String file;

    Gif(String file) {
        this.file = file;
    }

    public String getFile() {
        return file;
    }
}
