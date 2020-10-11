package pl.w93c.kafkaxjmeter.helpers;

public class ParamsParser {

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

}
