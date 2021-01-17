package pl.w93c.kafkaxjmeter.examples;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.joda.time.DateTime;
import pl.w93c.kafkaxjmeter.producers.KafkaxSpecificAvroProducer;

import java.math.BigDecimal;
import java.util.Map;

import static pl.w93c.kafkaxjmeter.helpers.ParamsParser.*;

public class ComplexExampleProducer extends KafkaxSpecificAvroProducer<ComplexExample> {

    @Override
    protected ComplexExample createSpecificRecord(JavaSamplerContext context) {

        final String brs = getParam(context, BOOLEAN_R);
        boolean br = "true".equalsIgnoreCase(brs);
        final String bos = getParam(context, BOOLEAN_O);
        Boolean bo = bos == null ? null : "true".equalsIgnoreCase(bos);

        String sr = getParam(context, STRING_R);
        String so = getParam(context, STRING_O);

        Long lr = toLong(getParam(context, STRING_R));
        String los = getParam(context, LONG_O);
        Long lo = isEmpty(los) ? null : toLong(los);

        DateTime dtr = toDateTime(getParam(context, TIMESTAMP_R)).toDateTime();
        String tss = getParam(context, TIMESTAMP_O);
        DateTime dto = isEmpty(tss) ? null : toDateTime(tss).toDateTime();

        final BigDecimal bd = toDecimal(getParam(context, BIG_DECIMAL_R));

        final String enum_s = getParam(context, ENUM_R);
        Counting cnt = Counting.valueOf(enum_s);
        if (cnt == null) {
            cnt = Counting.ENE;
        }

        return ComplexExample.newBuilder()
                .setBooleanRequired(br)
                .setBooleanOptional(bo)
                .setLongRequired(lr)
                .setLongOptional(lo)
                .setStringRequired(sr)
                .setStringOptional(so)
                .setTimestampMillisRequired(dtr)
                .setTimestampMillisOptional(dto)
                .setBigDecimalRequired(bd)
                .setEnumRequired(cnt)
                .build();

    }

    public static final String BOOLEAN_R = "ce.booleanRequired";
    public static final String BOOLEAN_O = "ce.booleanOptional";
    public static final String LONG_R = "ce.longRequired";
    public static final String LONG_O = "ce.longOptional";
    public static final String STRING_R = "ce.stringRequired";
    public static final String STRING_O = "ce.stringOptional";
    public static final String TIMESTAMP_R = "ce.timestampRequired";
    public static final String TIMESTAMP_O = "ce.tmestampOptional";
    public static final String BIG_DECIMAL_R = "ce.bigDecimalRequired";
    public static final String ENUM_R = "ce.enumRequired";

    @Override
    protected void populateParams(Map<String, String> map) {
        super.populateParams(map);
        map.put(BOOLEAN_R, "true");
        map.put(BOOLEAN_O, EMPTY_VALUE);
        map.put(LONG_R, "123456");
        map.put(LONG_O, EMPTY_VALUE);
        map.put(STRING_R, "Ene due rike fake");
        map.put(STRING_O, EMPTY_VALUE);
        map.put(TIMESTAMP_R, "2021-01-17T15:13:22.672Z");
        map.put(TIMESTAMP_O, EMPTY_VALUE);
        map.put(BIG_DECIMAL_R, "3.14");
        map.put(ENUM_R, "RIKE");
    }



}
