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

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import pl.w93c.kafkaxjmeter.KafkaxSampler;
import pl.w93c.kafkaxjmeter.run.KafkaxRun;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static pl.w93c.kafkaxjmeter.helpers.ParamsParser.isEmpty;

public abstract class KafkaxProducer extends KafkaxSampler {

    /** Optional parameter for setting the partition
     */
    protected static final String KAFKA_PARTITION = "kafka_partition";
    /** Optional parameter for setting the Kafka record key
     */
    protected static final String KAFKA_KEY = "kafka_key";

    private KafkaProducer<String, byte[]> producer;
    private Exception setupTestException;

    @Override
    protected void populateParams(Map<String, String> map) {
        super.populateParams(map);
        map.put(KAFKA_PARTITION, EMPTY_VALUE);
        map.put(KAFKA_KEY, EMPTY_VALUE);
    }

    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        if (isMock()) {
            producer = null;
        } else {
            try {
                producer = new KafkaProducer<>(getProps());
            }
            catch (Exception e) {
                // we can only save this exception to throw it in runTest, so JMeter can show it in GUI
                setupTestException = e;
            }
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

        if (setupTestException != null) {
            throw setupTestException;
        }

        final String topic = context.getParameter(TOPIC);
        final String key = context.getParameter(KAFKA_KEY);
        final String partitionString = context.getParameter(KAFKA_PARTITION);

        kafkaxRun.getKafkaParameters().setTopic(topic);

        final ProducerRecord<String, byte[]> producerRecord;
        final byte[] bytes = getBytes(context);
        final String message = getMessage(context);
        Long offset = null;

        if (!isMock() && producer != null) {
            if (isEmpty(partitionString)) {
                producerRecord = new ProducerRecord<>(topic, key, bytes);
            } else {
                final int partitionNumber = Integer.parseInt(partitionString);
                producerRecord = new ProducerRecord<>(topic, partitionNumber, key, bytes);
            }
            Future<RecordMetadata> result = producer.send(producerRecord);
            RecordMetadata meta;
            try {
                meta = result.get(1000, TimeUnit.MILLISECONDS);
                offset = meta.offset();
            }
            catch (InterruptedException | ExecutionException | TimeoutException e) {
                offset = null;
            }
        }

        addResult(kafkaxRun
                , 0 // do not change, value 0 is OK, there is only one record
                , offset, key, message, bytes
                , System.currentTimeMillis());

        kafkaxRun.getPostconditions().setRecordCount(1);
        kafkaxRun.getPostconditions().setSize((long)bytes.length);
    }

    protected abstract byte[] getBytes(JavaSamplerContext context) throws Exception;

    protected abstract String getMessage(JavaSamplerContext context);

}
