package pl.w93c.kafkaxjmeter.consumers;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;

public abstract class KafkaxSpecificAvroConsumer<T extends SpecificRecord> extends KafkaxConsumer {

    final Class<T> typeParameterClass = getParameterClass();

    protected abstract Class<T> getParameterClass();

    @Override
    protected final String processRecord(byte[] value) throws Exception {
        T t = deserializeT(value);
        processRecord(t);
        return t.toString();
    }

    protected abstract void processRecord(T t);

    protected T deserializeT(byte[] data) throws Exception {
        DatumReader<T> reader
                = new SpecificDatumReader<>(typeParameterClass);
        Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        return reader.read(null, decoder);
    }

}
