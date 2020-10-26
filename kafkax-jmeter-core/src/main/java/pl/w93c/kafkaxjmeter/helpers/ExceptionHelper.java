package pl.w93c.kafkaxjmeter.helpers;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHelper {
    /**
     * Return the stack trace as a string.
     *
     * @param exception the exception containing the stack trace
     * @return the stack trace
     */
    public static String getStackTrace(Exception exception) {
        // https://www.baeldung.com/java-stacktrace-to-string
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

}