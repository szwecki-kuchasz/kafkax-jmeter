package pl.w93c.kafkaxjmeter.producers;

import org.apache.avro.Conversions;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class KafkaxSpecificAvroProducer<T extends SpecificRecord> extends KafkaxProducer {

    @Override
    protected byte[] getBytes(JavaSamplerContext context) throws IOException {
        T record = getSpecificRecord(context);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(stream, null);
        SpecificData specificData = new SpecificData();
        specificData.addLogicalTypeConversion(new Conversions.DecimalConversion());
        DatumWriter<T> datumWriter = new SpecificDatumWriter<T>(record.getSchema(), specificData);
        datumWriter.write(record, encoder);
        encoder.flush();
        return stream.toByteArray();
    }

    @Override
    protected String getMessage(JavaSamplerContext context) {
        return getSpecificRecord(context).toString();
    }

    private T specificRecord;

    protected final T getSpecificRecord(JavaSamplerContext context) {
        if (specificRecord == null) {
             specificRecord = createSpecificRecord(context);
        }
        return specificRecord;
    }

    protected abstract T createSpecificRecord(JavaSamplerContext context);

}
