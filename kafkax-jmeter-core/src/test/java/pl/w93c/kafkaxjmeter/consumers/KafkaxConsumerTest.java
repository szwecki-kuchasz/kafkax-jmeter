package pl.w93c.kafkaxjmeter.consumers;

import org.apache.jmeter.config.Arguments;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaxConsumerTest {

    private static final String[] EXPECTED_PARAMS = {
            "kafka_brokers",
            "kafka_topic",
            "consumer_group",
            "kafka_compression_type",
            "kafka_mock",
            "kafka_use_ssl",
            "kafka_ssl_keystore", "kafka_ssl_keystore_password", "kafka_ssl_truststore", "kafka_ssl_truststore_password",
            "consumer_continue_at_fail",
            "consumer_poll_records_limit",
            "consumer_poll_time_msec",
            "consumer_total_poll_time_msec",
            "consumer_from_beginning",
            "consumer_client_id"
    };

    @Test
    public void shouldGetDefaultParameters() {
        final KafkaxConsumer consumer = new KafkaxConsumer() {
            @Override
            protected String processRecord(byte[] value) throws Exception {
                return null;
            }
        };
        Arguments arguments = consumer.getDefaultParameters();
        List<String> names = new ArrayList<>(arguments.getArgumentCount());
        arguments.getArguments().forEach(jMeterProperty -> names.add(jMeterProperty.getName()));
        assertThat(names)
                .contains(EXPECTED_PARAMS)
                .doesNotContain("kafka_partition")
                .doesNotContain("kafka_key")
        ;
    }

}