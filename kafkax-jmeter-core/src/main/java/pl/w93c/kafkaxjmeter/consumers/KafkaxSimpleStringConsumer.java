package pl.w93c.kafkaxjmeter.consumers;

public class KafkaxSimpleStringConsumer extends KafkaxConsumer {

    @Override
    protected String processRecord(byte[] value) {
        return new String(value);
    }

}

