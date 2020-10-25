/*
 * Copyright 2014 Signal.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.w93c.kafkaxjmeter.producers;

//import com.google.common.base.Strings;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import pl.w93c.kafkaxjmeter.KafkaxSampler;
import pl.w93c.kafkaxjmeter.run.KafkaxRun;

import java.util.Map;

import static pl.w93c.kafkaxjmeter.helpers.ParamsParser.isEmpty;

/**
 * A {@link org.apache.jmeter.samplers.Sampler Sampler} which produces Kafka messages.
 *
 * @author codyaray
 * @see "http://ilkinbalkanay.blogspot.com/2010/03/load-test-whatever-you-want-with-apache.html"
 * @see "http://newspaint.wordpress.com/2012/11/28/creating-a-java-sampler-for-jmeter/"
 * @see "http://jmeter.512774.n5.nabble.com/Custom-Sampler-Tutorial-td4490189.html"
 * <p>
 * Modifications by Andrzej Ligudziński aligudzinski@gmail.com
 * class and some methods made abstract for more elastic class hierarchy
 * package name changed, was: co.signal.pl.w93c.kafkameter
 * @since 6/27/14
 */
public abstract class KafkaxProducer extends KafkaxSampler {

    /**
     * Parameter for setting the partition. It is optional.
     */
    protected static final String PARAMETER_KAFKA_PARTITION = "kafka_partition";

    private KafkaProducer<String, byte[]> producer;

    @Override
    protected void populateParams(Map<String, String> map) {
        super.populateParams(map);
        map.put(PARAMETER_KAFKA_PARTITION, null);
    }

    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        if (isMock()) {
            producer = null;
        } else {
            producer = new KafkaProducer<>(getProps());
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (producer != null) {
            producer.close();
        }
        producer = null;
        super.teardownTest(context);
    }

    @Override
    protected void runTestImpl(JavaSamplerContext context, KafkaxRun kafkaxRun) throws Exception {
        final String topic = context.getParameter(PARAMETER_KAFKA_TOPIC);
        final String key = context.getParameter(PARAMETER_KAFKA_KEY);
        final String partitionString = context.getParameter(PARAMETER_KAFKA_PARTITION);

        kafkaxRun.getKafkaParameters().setTopic(topic);
// TODO save partition to kafkaxRun

        final ProducerRecord<String, byte[]> producerRecord;
        final byte[] bytes = getBytes(context);
        final String message = getMessage(context);

        addKafkaxRunPayload(kafkaxRun
                , 0 // do not change, value 0 is OK, there is only one record
                , key, message, bytes
                , Long.valueOf(0) // maybe we should know Producent offset
        );

        if (!isMock() && producer != null) {
            if (isEmpty(partitionString)) {
                producerRecord = new ProducerRecord<>(topic, key, bytes);
            } else {
                final int partitionNumber = Integer.parseInt(partitionString);
                producerRecord = new ProducerRecord<>(topic, partitionNumber, key, bytes);
            }
            producer.send(producerRecord);
        }
    }

    protected abstract byte[] getBytes(JavaSamplerContext context) throws Exception;

    protected abstract String getMessage(JavaSamplerContext context);

}
