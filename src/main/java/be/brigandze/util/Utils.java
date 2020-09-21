package be.brigandze.util;

import java.util.regex.Pattern;

public class Utils {
    private Utils() {
    }

    private static final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}
