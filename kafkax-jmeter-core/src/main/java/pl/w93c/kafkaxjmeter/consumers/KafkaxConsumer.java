package pl.w93c.kafkaxjmeter.consumers;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
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

import static pl.w93c.kafkaxjmeter.helpers.ParamsParser.toInt;
import static pl.w93c.kafkaxjmeter.helpers.ParamsParser.toLong;

public abstract class KafkaxConsumer extends KafkaxSampler {

    protected static final String PARAMETER_KAFKA_CONSUMER_GROUP = "consumer_group";
    protected static final String LIMIT = "consumer_poll_records_limit";
    protected static final String POLL_TIME = "consumer_poll_time_msec";
    protected static final String TOTAL_POLL_TIME = "consumer_total_poll_time_msec";
    protected static final String PARAMETER_KAFKA_CONSUMER_CONTINUE_AT_FAIL = "consumer_continue_at_fail";

    protected static final long DEFAULT_POLL_TIME = 5000L; // msec
    private String consumerGroup;
    private boolean continueAtFail;

    @Override
    protected void populateParams(Map<String, String> map) {
        super.populateParams(map);
        map.put(PARAMETER_KAFKA_CONSUMER_GROUP, "${PARAMETER_KAFKA_CONSUMER_GROUP}");
        map.put(LIMIT, "${PARAMETER_KAFKA_CONSUMER_POLL_RECORDS_LIMIT}");
        map.put(POLL_TIME, "${PARAMETER_KAFKA_CONSUMER_POLL_TIME}");
        map.put(TOTAL_POLL_TIME, "${PARAMETER_KAFKA_CONSUMER_TOTAL_POLL_TIME}");
        map.put(PARAMETER_KAFKA_CONSUMER_CONTINUE_AT_FAIL, "true");
    }

    KafkaConsumer<String, byte[]> consumer;

    private Exception setupTestException;

    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        setupTestException = null;
        consumerGroup = getParam(context, PARAMETER_KAFKA_CONSUMER_GROUP);
        getProps().put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        if (isMock()) {
            consumer = null;
        } else {
            try {
                consumer = new KafkaConsumer<>(getProps());
            }
            catch (Exception e) {
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

        String topic = getParam(context, PARAMETER_KAFKA_TOPIC);
        List<String> topics = getTopics(topic);

        continueAtFail = Boolean.TRUE.toString().equalsIgnoreCase(context.getParameter(PARAMETER_KAFKA_CONSUMER_CONTINUE_AT_FAIL));
        
        kafkaxRun.getKafkaParameters().setTopic(topic);
        kafkaxRun.getPreconditions().setConsumerContinueAtFail(continueAtFail);
        kafkaxRun.getPreconditions().setConsumerPollTime(pollTime);
        kafkaxRun.getPreconditions().setConsumerTotalPollTime(totalPollTime);
        kafkaxRun.getPreconditions().setConsumerRecordLimit(limit);

        if (!isMock() && consumer != null) {
            consumer.subscribe(topics);
            int totalRecords = 0;
            int errorCount = 0;
            long totalSize = 0;
            long startTime = System.currentTimeMillis(), stopTime = startTime;
            Exception savedException = null;

            boolean enough = false, timeIsOver = false, broken = false;

            WHOLE_LOOP: while (!timeIsOver
                    && !enough
            ) {
                ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofMillis(pollTime));
                int recCount = consumerRecords.count();
                if (recCount > 0) {
                    for (ConsumerRecord<String, byte[]> record : consumerRecords) {
                        totalSize += record.value().length;
                        try {
                            processRecord(totalRecords, record.key(), record.value(), kafkaxRun, record.offset());
                        }
                        catch (Exception e) {
                            savedException = e;
                            errorCount++;
                            if (!continueAtFail) {
                                broken = true;
                                break WHOLE_LOOP;
                            }
                        }
                    }
                    if ((totalRecords += recCount) >= limit) {
                        enough = true;
                        break;
                    };
                }
                stopTime = System.currentTimeMillis();
                timeIsOver = stopTime > startTime + totalPollTime;
            };

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

    protected abstract void processRecord(int recordNumber, String key, byte[] value, KafkaxRun kafkaxRun, Long offset) throws Exception;

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
