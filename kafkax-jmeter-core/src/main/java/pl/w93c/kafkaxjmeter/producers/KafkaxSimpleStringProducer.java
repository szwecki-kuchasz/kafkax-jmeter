package pl.w93c.kafkaxjmeter.producers;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import java.util.Map;

public final class KafkaxSimpleStringProducer extends KafkaxStringProducer {

    /**
     * Parameter for setting the Kafka message.
     */
    private static final String PARAMETER_KAFKA_MESSAGE = "kafka_message";

    @Override
    protected String getMessage(JavaSamplerContext context) {
        return context.getParameter(PARAMETER_KAFKA_MESSAGE);
    }

    @Override
    protected void populateParams(Map<String, String> map) {
        super.populateParams(map);
        map.put(PARAMETER_KAFKA_MESSAGE, EMPTY_VALUE);
    }

}
