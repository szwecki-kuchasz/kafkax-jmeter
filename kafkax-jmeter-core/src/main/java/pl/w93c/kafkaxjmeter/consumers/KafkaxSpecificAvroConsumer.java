package pl.w93c.kafkaxjmeter.consumers;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import pl.w93c.kafkaxjmeter.run.KafkaxRun;

public abstract class KafkaxSpecificAvroConsumer<T extends SpecificRecord> extends KafkaxConsumer {

    final Class<T> typeParameterClass = getParameterClass();

    protected abstract Class<T> getParameterClass();

    @Override
    protected void processRecord(int recordNumber, String key, byte[] value, KafkaxRun kafkaxRun, Long offset) throws Exception {
        try {
            T t = deserializeT(value);
            processRecord(t);
            // kafkaxRun.setResponseData(t.toString());
            addKafkaxRunPayload(kafkaxRun, recordNumber, key, t.toString(), value, offset);
        } catch (Exception e) {
            // kafkaxRun.setResponseCode(e.getMessage());
            addKafkaxRunPayload(kafkaxRun, recordNumber, key, e.getClass().getName() + ":/n" + e.getMessage(), value, null);
            throw e;
        }
    }

    protected abstract void processRecord(T t);

    private T deserializeT(byte[] data) throws Exception {
        DatumReader<T> reader
                = new SpecificDatumReader<>(typeParameterClass);
        Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        return reader.read(null, decoder);
    }

}
