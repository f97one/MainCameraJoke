package net.formula97.android.app_maincamerajoke;

/**
 * Created by HAJIME on 2014/08/03.
 */
public class StringUtils {
    public static boolean isNullOrEmpty(String testString) {
        boolean ret = true;

        if (testString != null && testString.length() > 0) {
            ret = false;
        }

        return ret;
    }
}
