package be.brigandze.control;

import static java.lang.System.getProperty;

public interface OperatingSystemDependent {

    default boolean isWindows() {
        return getProperty("os.name").toLowerCase().contains("windows");
    }
}
