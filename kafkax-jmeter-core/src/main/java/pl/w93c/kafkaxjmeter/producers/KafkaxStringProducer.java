package pl.w93c.kafkaxjmeter.producers;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

public abstract class KafkaxStringProducer extends KafkaxProducer {

    @Override
    protected byte[] getBytes(JavaSamplerContext context) {
        return getMessage(context).getBytes();
    }

}
