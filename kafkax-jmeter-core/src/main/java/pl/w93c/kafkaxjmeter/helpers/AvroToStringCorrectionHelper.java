package pl.w93c.kafkaxjmeter.helpers;

import java.util.regex.Pattern;

public class AvroToStringCorrectionHelper {

    // 2020-11-19T21:17:30.985+01:00
    private static final Pattern TIME_STAMP_PATTERN
            = Pattern.compile(":\\w*[^\"](\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{2}:\\d{2})([^\"])");

    /*
    private static final Pattern TIME_PATTERN = Pattern.compile(":\\w*[^\"](\\d{2}:\\d{2}:\\d{2}.\\d{3})([^\"])");
    // np.: ": 14:40:47.905},
    private static final Pattern DATE_PATTERN = Pattern.compile(":\\w*[^\"](\\d{4}-\\d{2}-\\d{2})([^\"])");
    // np.: ": 1970-04-25,
    // np.: ": 2019-08-22}
     */

    public static String correctAvroTimeStamp(String input) {
        return TIME_STAMP_PATTERN.matcher(input).replaceAll(": \"$1\"$2");
    }

}
