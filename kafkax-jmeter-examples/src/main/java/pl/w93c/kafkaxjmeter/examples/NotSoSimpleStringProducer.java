package pl.w93c.kafkaxjmeter.examples;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import pl.w93c.kafkaxjmeter.producers.KafkaxStringProducer;

import java.util.Map;

public class NotSoSimpleStringProducer extends KafkaxStringProducer {

    public static final String PATTERN = "My name is %s %s,\n" +
            "I come from %s\n" +
            "I work in a lumber mill there\n" +
            "All the people I meet\n" +
            "As I walk down the street\n" +
            "Ask me how in the hell I got there\n" +
            "So I tell them: My name is %s %s...";
    public static final String NOT_SO_SIMPLE_FIRST_NAME = "not.so.simple.first.name";
    public static final String NOT_SO_SIMPLE_LAST_NAME = "not.so.simple.last.name";
    public static final String NOT_SO_SIMPLE_PLACE = "not.so.simple.place";

    @Override
    protected void populateParams(Map<String, String> map) {
        super.populateParams(map);
        map.put(NOT_SO_SIMPLE_FIRST_NAME, "Yon");
        map.put(NOT_SO_SIMPLE_LAST_NAME, "Yonson");
        map.put(NOT_SO_SIMPLE_PLACE, "Wisconsin");
    }

    @Override
    protected String getMessage(JavaSamplerContext context) {
        final String firstName = getParam(context, NOT_SO_SIMPLE_FIRST_NAME);
        final String lastName = getParam(context, NOT_SO_SIMPLE_LAST_NAME);
        final String place = getParam(context, NOT_SO_SIMPLE_PLACE);
        return String.format(PATTERN,
                firstName, lastName, place, firstName, lastName);
    }

}
