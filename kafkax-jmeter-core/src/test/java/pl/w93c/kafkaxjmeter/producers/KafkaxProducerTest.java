package pl.w93c.kafkaxjmeter.producers;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaxProducerTest {

    private static final String[] EXPECTED_PARAMS = {
            "kafka_brokers",
            "kafka_topic",
            "kafka_compression_type",
            "kafka_key",
            "kafka_mock",
            "kafka_partition",
            "kafka_use_ssl",
            "kafka_ssl_keystore", "kafka_ssl_keystore_password", "kafka_ssl_truststore", "kafka_ssl_truststore_password"
    };
    public static final String DO_NOT_USE_THIS_METHOD = "Don't use this method";

    @Test
    public void shouldGetDefaultParameters() {
        final KafkaxProducer producer = new KafkaxProducer() {
            @Override
            protected byte[] getBytes(JavaSamplerContext context) throws Exception {
                throw new RuntimeException(DO_NOT_USE_THIS_METHOD);
            }

            @Override
            protected String getMessage(JavaSamplerContext context) {
                throw new RuntimeException(DO_NOT_USE_THIS_METHOD);
            }
        };
        Arguments arguments = producer.getDefaultParameters();
        List<String> names = new ArrayList<>(arguments.getArgumentCount());
        arguments.getArguments().forEach(jMeterProperty -> names.add(jMeterProperty.getName()));
        assertThat(names)
                .contains(EXPECTED_PARAMS)
                .doesNotContain("kafka_consumer_group")
        ;
    }

}