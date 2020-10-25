package pl.w93c.kafkaxjmeter;

import org.apache.jmeter.samplers.SampleResult;
import pl.w93c.kafkaxjmeter.run.KafkaxRun;

public class KafkaxSamplerResult extends SampleResult {

    private final KafkaxRun header = new KafkaxRun();

    public KafkaxRun getKafkaxRun() {
        return header;
    }

}
