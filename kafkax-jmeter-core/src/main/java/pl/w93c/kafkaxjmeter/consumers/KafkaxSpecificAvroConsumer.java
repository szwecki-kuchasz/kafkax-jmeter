package pl.w93c.kafkaxjmeter.consumers;

import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import pl.w93c.kafkaxjmeter.helpers.ExceptionHelper;
import pl.w93c.kafkaxjmeter.run.KafkaxRun;

public abstract class KafkaxSpecificAvroConsumer<T extends SpecificRecord> extends KafkaxConsumer {

    final Class<T> typeParameterClass = getParameterClass();

    protected abstract Class<T> getParameterClass();

    @Override
    protected final void processRecord(int recordNumber, String key, byte[] value, KafkaxRun kafkaxRun, Long offset) throws Exception {
        try {
            T t = deserializeT(value);
            processRecord(t);
            addKafkaxRunPayload(kafkaxRun, recordNumber, key, t.toString(), value, offset);
        } catch (Exception e) {
            addKafkaxRunPayload(kafkaxRun, recordNumber, key, ExceptionHelper.getStackTrace(e), value, offset);
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
