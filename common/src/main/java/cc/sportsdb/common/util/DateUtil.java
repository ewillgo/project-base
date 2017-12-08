package cc.sportsdb.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class DateUtil {

    private static final String FULL_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private DateUtil() {
    }

    public static String now() {
        return now(FULL_PATTERN);
    }

    public static String now(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }
}
