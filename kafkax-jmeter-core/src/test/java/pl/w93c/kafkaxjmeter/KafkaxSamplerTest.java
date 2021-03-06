package pl.w93c.kafkaxjmeter;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.joda.time.DateTime;
import org.junit.Test;
import pl.w93c.kafkaxjmeter.helpers.KafkaxRunJsonSplitter;
import pl.w93c.kafkaxjmeter.run.KafkaParameters;
import pl.w93c.kafkaxjmeter.run.KafkaxPostconditions;
import pl.w93c.kafkaxjmeter.run.KafkaxPreconditions;
import pl.w93c.kafkaxjmeter.run.KafkaxRun;

import java.util.ArrayList;

public class KafkaxSamplerTest {

    @Test
    public void createRun() {
        KafkaxRun run = KafkaxRun.newBuilder()
                .setKafkaParameters(KafkaParameters.newBuilder()
                        .setBrokers("broker.com:9093")
                        .setTopic("ourTopic")
                        .setMock(false)
                        .setStartTime(new DateTime())
                        .setEndTime(null)
                        .setSsl(true)
                        .build()
                )
                .setPayload(new ArrayList<>())
                .setErrors(new ArrayList<>())
                .setPreconditions(KafkaxPreconditions.newBuilder()
                        .setConsumerContinueAtFail(null)
                        .setConsumerPollTime(null)
                        .setConsumerRecordLimit(null)
                        .setConsumerTotalPollTime(null)
                        .setConsumerGroup(null)
                        .setClientId(null)
                        .setConsumerFromBeginning(null)
                        .build()
                )
                .setPostconditions(KafkaxPostconditions.newBuilder()
                        .setSize(0)
                        .setRecordCount(0)
                        .setErrorCount(null)
                        .setEndConditionCount(null)
                        .setEndConditionTime(null)
                        .setEndConditionException(null)
                        .build()
                )
                .build();
        KafkaxSampler sampler = new KafkaxSampler() {
            @Override
            protected void runTestImpl(JavaSamplerContext context, KafkaxRun kafkaxRun) {
                // NOP
            }
        };
        for (int i = 0 ; i < 4; i++) {
            String s = "value" + i;
            sampler.addResult(run, i, null, "key" + i, s, s.getBytes(), System.currentTimeMillis());
        }
        try {
            throw new IllegalStateException("nevermind");
        } catch (Exception expected) {
            sampler.addError(run, 4, null, expected, "NeverMind".getBytes());
        }

        System.out.println(KafkaxRunJsonSplitter.printKafkaxRun(run));

    }

}