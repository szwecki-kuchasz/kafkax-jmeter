package pl.w93c.kafkaxjmeter.consumers;

import pl.w93c.kafkaxjmeter.serde.SerdeData;

public class DummyAvroConsumer extends KafkaxSpecificAvroConsumer<SerdeData> {


    public SerdeData getDeserialized(byte[] value) throws Exception {
        return deserializeT(value);
    }


    @Override
    protected Class<SerdeData> getParameterClass() {
        return SerdeData.class;
    }

    @Override
    protected void processRecord(SerdeData serdeData) {
        System.out.println(serdeData.toString());
    }
}
