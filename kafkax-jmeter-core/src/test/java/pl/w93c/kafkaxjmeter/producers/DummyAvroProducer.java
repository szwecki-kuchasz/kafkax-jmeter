package pl.w93c.kafkaxjmeter.producers;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import pl.w93c.kafkaxjmeter.serde.Counting;
import pl.w93c.kafkaxjmeter.serde.SerdeData;

import java.io.IOException;
import java.math.BigDecimal;

public class DummyAvroProducer extends KafkaxSpecificAvroProducer<SerdeData> {
    @Override
    protected SerdeData createSpecificRecord(JavaSamplerContext context) {
        return
                SerdeData.newBuilder()
                        .setBooleanRequired(true)
                        .setBooleanOptional(false)
                        .setLongRequired(Long.MAX_VALUE)
                        .setLongOptional(Long.MIN_VALUE)
                        .setStringRequired("Lorem ipsum")
                        .setStringOptional("Dolor sit amet")
                        .setTimestampMillisRequired(DateTime.parse("2021-01-17T15:13:22.672Z"))
                        .setTimestampMillisOptional(null)
                        .setBigDecimalRequired(new BigDecimal("3.14"))
                        .setEnumRequired(Counting.RIKE)
                        .setDateRequired(new LocalDate())
                        .setTimeRequired(new LocalTime().getMillisOfDay())
                        .setUuidRequired("tisNotUUIDAtAll...")
                        .build();
    }

    public byte[] getSerialized() throws IOException {
        beforeRun(null, new SampleResult(), null);
        return getBytes(null);
    }


}
