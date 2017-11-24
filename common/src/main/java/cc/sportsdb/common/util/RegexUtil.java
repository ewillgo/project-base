package cc.sportsdb.common.util;

import java.util.regex.Pattern;

public abstract class RegexUtil {

    public static final Pattern UUID = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static boolean isMatch(String str, Pattern pattern) {
        return pattern.matcher(str).matches();
    }
}
