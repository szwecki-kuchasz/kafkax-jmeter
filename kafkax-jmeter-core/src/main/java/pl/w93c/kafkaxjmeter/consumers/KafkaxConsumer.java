package pl.w93c.kafkaxjmeter.consumers;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import pl.w93c.kafkaxjmeter.KafkaxSampler;
import pl.w93c.kafkaxjmeter.run.KafkaxRun;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import static pl.w93c.kafkaxjmeter.helpers.ParamsParser.*;

public abstract class KafkaxConsumer extends KafkaxSampler {

    protected static final String CONSUMER_GROUP = "consumer_group";
    protected static final String CLIENT_ID = "consumer_client_id";
    protected static final String LIMIT = "consumer_poll_records_limit";
    protected static final String POLL_TIME = "consumer_poll_time_msec";
    protected static final String TOTAL_POLL_TIME = "consumer_total_poll_time_msec";
    protected static final String CONTINUE_AT_FAIL = "consumer_continue_at_fail";
    protected static final String FROM_BEGINNING = "consumer_from_beginning";

    protected static final long DEFAULT_POLL_TIME = 5000L; // msec
    private String consumerGroup;
    private String clientId;
    private boolean fromBeginning;

    @Override
    protected void populateParams(Map<String, String> map) {
        super.populateParams(map);
        map.put(CONSUMER_GROUP, "${PARAMETER_KAFKA_CONSUMER_GROUP}");
        map.put(LIMIT, "${PARAMETER_KAFKA_CONSUMER_POLL_RECORDS_LIMIT}");
        map.put(POLL_TIME, "${PARAMETER_KAFKA_CONSUMER_POLL_TIME}");
        map.put(TOTAL_POLL_TIME, "${PARAMETER_KAFKA_CONSUMER_TOTAL_POLL_TIME}");
        map.put(CONTINUE_AT_FAIL, "true");
        map.put(FROM_BEGINNING, "false");
        map.put(CLIENT_ID, EMPTY_VALUE);
    }

    private KafkaConsumer<String, byte[]> consumer;

    private Exception setupTestException;

    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        setupTestException = null;
        consumerGroup = getParam(context, CONSUMER_GROUP);
        getProps().put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        clientId = getParam(context, CLIENT_ID);
        if (!isEmpty(clientId)) {
            getProps().put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        }
        fromBeginning =
                Boolean.TRUE.toString().equalsIgnoreCase(context.getParameter(FROM_BEGINNING));
        getProps().put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                fromBeginning
                        ? "earliest"
                        : "latest");

        if (isMock()) {
            consumer = null;
        } else {
            try {
                consumer = new KafkaConsumer<>(getProps());
            } catch (Exception e) {
                getNewLogger().warn("Create KafkaConsumer failed", e);
                // we can only save this exception to throw it in runTest, so JMeter can show it in GUI
                setupTestException = e;
            }
        }
    }

    @Override
    protected void runTestImpl(JavaSamplerContext context, KafkaxRun kafkaxRun) throws Exception {

        if (setupTestException != null) {
            throw setupTestException;
        }

        long pollTime = getPollTime(context);
        long totalPollTime = getTotalPollTime(context);
        int limit = getRecordLimit(context);
        boolean needFirstPoll = true;

        String topic = getParam(context, TOPIC);
        List<String> topics = getTopics(topic);

        boolean continueAtFail =
                Boolean.TRUE.toString().equalsIgnoreCase(context.getParameter(CONTINUE_AT_FAIL));

        kafkaxRun.getKafkaParameters().setTopic(topic);
        kafkaxRun.getPreconditions().setConsumerContinueAtFail(continueAtFail);
        kafkaxRun.getPreconditions().setConsumerPollTime(pollTime);
        kafkaxRun.getPreconditions().setConsumerTotalPollTime(totalPollTime);
        kafkaxRun.getPreconditions().setConsumerRecordLimit(limit);
        kafkaxRun.getPreconditions().setConsumerGroup(consumerGroup);
        kafkaxRun.getPreconditions().setClientId(clientId);
        kafkaxRun.getPreconditions().setConsumerFromBeginning(fromBeginning);

        if (!isMock() && consumer != null) {
            consumer.subscribe(topics);
            int totalRecords = 0, errorCount = 0;
            long totalSize = 0;
            long startTime = System.currentTimeMillis(), stopTime;
            boolean enough = false, timeIsOver = false, broken = false;
            Exception savedException = null;

            WHOLE_LOOP:
            while (!timeIsOver
                    && !enough
            ) {
                if (fromBeginning && needFirstPoll) {
                    needFirstPoll = false;
                }
                ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofMillis(pollTime));
                for (ConsumerRecord<String, byte[]> record : consumerRecords) {
                    final byte[] value = record.value();
                    totalSize += value.length;
                    final long offset = record.offset();
                    try {
                        final String key = record.key();
                        final String processed = processRecord(value);
                        addResult(kafkaxRun, totalRecords, offset, key, processed, value);
                        totalRecords++;
                    } catch (Exception e) {
                        addError(kafkaxRun, totalRecords, offset, e, value);
                        totalRecords++;
                        savedException = e;
                        errorCount++;
                        if (!continueAtFail) {
                            broken = true;
                            break WHOLE_LOOP;
                        }
                    }
                }
                // we should check this condition in previous 'for' loop, but some already downloaded records would be lost
                if (totalRecords >= limit) {
                    enough = true;
                }
                stopTime = System.currentTimeMillis();
                timeIsOver = stopTime > startTime + totalPollTime;
            }
            ;

            kafkaxRun.getPostconditions().setRecordCount(totalRecords);
            kafkaxRun.getPostconditions().setSize(totalSize);
            kafkaxRun.getPostconditions().setErrorCount(errorCount);
            kafkaxRun.getPostconditions().setEndConditionCount(enough);
            kafkaxRun.getPostconditions().setEndConditionTime(timeIsOver);
            kafkaxRun.getPostconditions().setEndConditionException(broken);

            if (errorCount > 1) {
                // tell JMeter about >=1 error during consuming
                throw new KafkaxConsumeException("Errors occurred while reading (" + errorCount + "). See details in Response Headers.");
            } else if (errorCount > 0) {
                // show original single exception as cause of test fail
                throw new KafkaxConsumeException(savedException);
            }

        }
    }

    private int getRecordLimit(JavaSamplerContext context) {
        int limit = toInt(getParam(context, LIMIT));
        if (limit <= 0) {
            limit = Integer.MAX_VALUE;
        }
        return limit;
    }

    private long getPollTime(JavaSamplerContext context) {
        long pollTime = toLong(getParam(context, POLL_TIME));
        if (pollTime <= 0) {
            pollTime = DEFAULT_POLL_TIME;
        }
        return pollTime;
    }

    private long getTotalPollTime(JavaSamplerContext context) {
        long pollTime = toLong(getParam(context, TOTAL_POLL_TIME));
        if (pollTime <= 0) {
            pollTime = DEFAULT_POLL_TIME;
        }
        return pollTime;
    }

    protected abstract String processRecord(byte[] value) throws Exception;

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (consumer != null) {
            consumer.close();
        }
        consumer = null;
        super.teardownTest(context);
    }

    private List<String> getTopics(String topic) {
        StringTokenizer st = new StringTokenizer(topic, ";,");
        List<String> result = new ArrayList<>();
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }

}
