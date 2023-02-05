package pl.w93c.kafkaxjmeter.examples;

import pl.w93c.kafkaxjmeter.consumers.KafkaxSpecificAvroConsumer;

public class ComplexExampleConsumer extends KafkaxSpecificAvroConsumer<ComplexExample> {
    @Override
    protected Class<ComplexExample> getParameterClass() {
        return ComplexExample.class;
    }

}
