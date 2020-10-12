package pl.w93c.kafkaxjmeter.examples;

import com.kitfox.svg.A;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class NotSoSimpleStringProducerTest {

    private static final String[] EXPECTED_PARAMS = {
            "kafka_brokers",
            "kafka_topic",
            "kafka_compression_type",
            "kafka_key",
            "kafka_mock",
            "kafka_partition",
            "kafka_use_ssl",
            "kafka_ssl_keystore", "kafka_ssl_keystore_password", "kafka_ssl_truststore", "kafka_ssl_truststore_password",
            NotSoSimpleStringProducer.NOT_SO_SIMPLE_FIRST_NAME,
            NotSoSimpleStringProducer.NOT_SO_SIMPLE_LAST_NAME,
            NotSoSimpleStringProducer.NOT_SO_SIMPLE_PLACE
    };

    @Test
    public void shouldGetDefaultParameters() {
        NotSoSimpleStringProducer producer = new NotSoSimpleStringProducer();
        Arguments arguments = producer.getDefaultParameters();
        List<String> names = new ArrayList<>(arguments.getArgumentCount());
        arguments.getArguments().forEach(jMeterProperty -> names.add(jMeterProperty.getName()));
        assertThat(names)
                .contains(EXPECTED_PARAMS)
                .doesNotContain("kafka_consumer_group")
        ;
    }

    @Test
    public void shouldGetMessageWithDefaultParameters() {
        NotSoSimpleStringProducer producer = new NotSoSimpleStringProducer();
        Arguments arguments = producer.getDefaultParameters();
        JavaSamplerContext context = new JavaSamplerContext(arguments);
        final String message = producer.getMessage(context);
        assertThat(message)
                .startsWith("My name is Yon Yonson")
                .contains("I come from Wisconsin")
                .endsWith("My name is Yon Yonson...")
                ;
    }

    @Test
    public void shouldGetMessageWithCustomParameters() {
        NotSoSimpleStringProducer producer = new NotSoSimpleStringProducer();
        Arguments arguments = new Arguments();
        arguments.addArgument(NotSoSimpleStringProducer.NOT_SO_SIMPLE_FIRST_NAME, "Alfons");
        arguments.addArgument(NotSoSimpleStringProducer.NOT_SO_SIMPLE_LAST_NAME, "van Worden");
        arguments.addArgument(NotSoSimpleStringProducer.NOT_SO_SIMPLE_PLACE, "Venta Quemada");
        JavaSamplerContext context = new JavaSamplerContext(arguments);
        final String message = producer.getMessage(context);
        assertThat(message)
                .startsWith("My name is Alfons van Worden")
                .contains("I come from Venta Quemada")
                .endsWith("My name is Alfons van Worden...")
                ;
    }
}