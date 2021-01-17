package pl.w93c.kafkaxjmeter;

import org.junit.Test;
import pl.w93c.kafkaxjmeter.consumers.DummyAvroConsumer;
import pl.w93c.kafkaxjmeter.producers.DummyAvroProducer;
import pl.w93c.kafkaxjmeter.serde.SerdeData;

import java.io.IOException;

import static org.junit.Assert.*;

public class KafkaxSpecificAvroSerdeTest {

    @Test
    public void shouldSerialize() throws IOException {
        final DummyAvroProducer producer = new DummyAvroProducer();
        byte[] serialized = producer.getSerialized();
        System.out.println("Serialized bytes: " + new String(serialized));
    }

    @Test
    public void shouldSerializeAndDeserialize() throws Exception {
        final DummyAvroProducer producer = new DummyAvroProducer();
        final DummyAvroConsumer consumer = new DummyAvroConsumer();
        byte[] bytes = producer.getSerialized();
        final SerdeData deserialized = consumer.getDeserialized(bytes);
        System.out.println(deserialized);
    }

}