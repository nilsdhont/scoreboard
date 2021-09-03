package be.brigandze.util;

import java.util.Objects;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");


    private Utils() {
    }

    public static boolean isNotNullString(Object o) {
        if (o instanceof String) {
            return !Objects.equals(o, "null");
        }
        return true;
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return numericPattern.matcher(strNum).matches();
    }
}
