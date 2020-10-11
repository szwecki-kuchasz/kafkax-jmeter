package pl.w93c.kafkaxjmeter.consumers;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.apache.jmeter.samplers.SampleResult;

public abstract class KafkaxSpecificAvroConsumer<T extends SpecificRecord> extends KafkaxConsumer {

    final Class<T> typeParameterClass = getParameterClass();

    protected abstract Class<T> getParameterClass();

    @Override
    protected void processRecord(int recordNumber, String key, byte[] value, SampleResult sampleResult) throws Exception {
        try {
            T t = deserializeT(value);
            processRecord(t);
            sampleResult.setResponseData(t.toString());
        } catch (Exception e) {
            sampleResult.setResponseCode(e.getMessage());
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
