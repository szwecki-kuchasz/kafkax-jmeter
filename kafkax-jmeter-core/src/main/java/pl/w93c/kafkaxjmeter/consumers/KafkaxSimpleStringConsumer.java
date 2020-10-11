package pl.w93c.kafkaxjmeter.consumers;

import org.apache.jmeter.samplers.SampleResult;

public class KafkaxSimpleStringConsumer extends KafkaxConsumer {

    @Override
    protected void processRecord(int recordNumber, String key, byte[] value, SampleResult sampleResult) {
        String s = "Process record " + recordNumber + ", key=" + key + ", value=" + new String(value);
        System.out.println("partially result: " + s);
        sampleResult.setResponseData(sampleResult.getResponseDataAsString() + '\n' + s);
    }

}

