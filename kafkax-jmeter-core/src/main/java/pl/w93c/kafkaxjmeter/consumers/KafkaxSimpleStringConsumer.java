package pl.w93c.kafkaxjmeter.consumers;

import pl.w93c.kafkaxjmeter.run.KafkaxRun;

public class KafkaxSimpleStringConsumer extends KafkaxConsumer {

    @Override
    protected void processRecord(int recordNumber, String key, byte[] value, KafkaxRun kafkaxRun, Long offset) {
        addKafkaxRunPayload(kafkaxRun, recordNumber, key, new String(value), value, offset);
    }

}

