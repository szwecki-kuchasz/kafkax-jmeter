package pl.w93c.kafkaxjmeter.producers;

import org.apache.avro.Conversions;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import pl.w93c.kafkaxjmeter.run.KafkaxRun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class KafkaxSpecificAvroProducer<T extends SpecificRecord> extends KafkaxProducer {

    private T specificRecord;

    @Override
    protected void beforeRun(JavaSamplerContext context, SampleResult sampleResult, KafkaxRun run) {
        super.beforeRun(context, sampleResult, run);
        specificRecord = createSpecificRecord(context);
    }

    @Override
    protected byte[] getBytes(JavaSamplerContext context) throws IOException {
        T record = getSpecificRecord(context);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(stream, null);
        SpecificData specificData = new SpecificData();
        specificData.addLogicalTypeConversion(new Conversions.DecimalConversion());
        SpecificDatumWriter<T> datumWriter = new SpecificDatumWriter<>(record.getSchema(), specificData);

        datumWriter.getSpecificData().addLogicalTypeConversion(new Conversions.DecimalConversion());

        datumWriter.write(record, encoder);
        encoder.flush();
        return stream.toByteArray();
    }

    private T getSpecificRecord(JavaSamplerContext context) {
        return specificRecord;
    }

    @Override
    protected String getMessage(JavaSamplerContext context) {
        return specificRecord.toString();
    }

    protected abstract T createSpecificRecord(JavaSamplerContext context);

}
