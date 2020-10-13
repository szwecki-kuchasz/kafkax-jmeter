package pl.w93c.kafkaxjmeter.helpers;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParamsParser {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static double toDouble(String text) {
        try {
            text = text.replace(',', '.');
            return Double.valueOf(text);
        } catch (NumberFormatException nfe) {
            return 0.0;
        }
    }

    public static double toFloat(String text) {
        try {
            text = text.replace(',', '.');
            return Float.valueOf(text);
        } catch (NumberFormatException nfe) {
            return 0.0;
        }
    }

    public static long toLong(String text) {
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException nfe) {
            return 0L;
        }
    }

    public static int toInt(String text) {
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    public static BigDecimal toDecimal(String text) {
        try {
            text = text.replace(',', '.');
            try {
                // najpierw próbujemy jako liczbe bez ułamka, żeby nie wprowadzać zbędnie scale = 1 z double'a
                return BigDecimal.valueOf(Long.valueOf(text));
            } catch (RuntimeException e1) {
                return BigDecimal.valueOf(Double.valueOf(text));
            }
        } catch (RuntimeException e) {
            return BigDecimal.ZERO;
        }
    }

    public static LocalDateTime toDateTime(String text) {
        try {
            Date dd = SIMPLE_DATE_FORMAT.parse(text);
            return new LocalDateTime(dd.getTime());
        } catch (Exception e) {
            return new LocalDateTime();
        }
    }

    public static LocalDate toDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (RuntimeException e) {
            return new LocalDate();
        }
    }

    public static LocalTime toTime(String text) {
        try {
            return LocalTime.parse(text);
        } catch (RuntimeException e) {
            return new LocalTime();
        }
    }

    public static String[] split(String param) {
        if(isEmpty(param)) {
            return new String[0];
        } else {
            return param.split(",");
        }
    }

    public static boolean isEmpty(String s) {
        return s == null || "".equals(s.trim());
    }

}