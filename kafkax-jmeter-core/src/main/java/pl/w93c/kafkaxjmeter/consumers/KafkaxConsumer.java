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

    /**
     * Parameter for Kafka consumer group.
     */
    protected static final String PARAMETER_KAFKA_CONSUMER_GROUP = "kafka_consumer_group";

    protected static final String LIMIT = "consumer_poll_records_limit";
    protected static final String POLL_TIME = "consumer_poll_time_msec";
    protected static final String TOTAL_POLL_TIME = "consumer_total_poll_time_msec";

    protected static final long DEFAULT_POLL_TIME = 5000L; // msec
    private String consumerGroup;

    @Override
    protected void populateParams(Map<String, String> map) {
        super.populateParams(map);
        map.put(PARAMETER_KAFKA_CONSUMER_GROUP, "${PARAMETER_KAFKA_CONSUMER_GROUP}");
        map.put(LIMIT, "${PARAMETER_KAFKA_CONSUMER_POLL_RECORDS_LIMIT}");
        map.put(POLL_TIME, "${PARAMETER_KAFKA_CONSUMER_POLL_TIME}");
        map.put(TOTAL_POLL_TIME, "${PARAMETER_KAFKA_CONSUMER__TOTAL_POLL_TIME}");
    }

    KafkaConsumer<String, byte[]> consumer;

    @Override
    public void setupTest(JavaSamplerContext context) {
        super.setupTest(context);
        consumerGroup = getParam(context, PARAMETER_KAFKA_CONSUMER_GROUP);
        getProps().put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        if (isMock()) {
            consumer = null;
        } else {
            consumer = new KafkaConsumer<>(getProps());
        }
    }

    @Override
    protected void runTestImpl(JavaSamplerContext context, KafkaxRun kafkaxRun) throws Exception {
        long pollTime = getPollTime(context);
        long totalPollTime = getTotalPollTime(context);
        int limit = getRecordLimit(context);

        String topic = getParam(context, PARAMETER_KAFKA_TOPIC);
        List<String> topics = getTopics(topic);

//        sampleResult.setRequestHeaders("topics:" + topic + "\nmax records:" + limit + "\n pollTime:" + pollTime + "\n totalPollTime:" + totalPollTime
//                + "\nmock:" + isMock());
        kafkaxRun.getKafkaParameters().setTopic(topic);

        if (!isMock() && consumer != null) {
            consumer.subscribe(topics);
            int totalRecords = 0;
            long totalSize = 0;
            long startTime = System.currentTimeMillis(), stopTime = startTime;

            StringBuilder sb = new StringBuilder();
            sb.append("start:").append(startTime);

            boolean enough = false, timeIsOver = false;

            while (!timeIsOver
                    && !enough
                    && true // TODO: look at whole scenario conditions
            ) {
                ConsumerRecords<String, byte[]> consumerRecords = consumer.poll(Duration.ofMillis(pollTime));
                int recCount = consumerRecords.count();
                if (recCount > 0) {
                    for (ConsumerRecord<String, byte[]> record : consumerRecords) {
                        totalSize += record.value().length;
                        processRecord(totalRecords, record.key(), record.value(), kafkaxRun, record.offset());
                    }
                    if ((totalRecords += recCount) >= limit) {
                        enough = true;
                        break;
                    };
                }
                stopTime = System.currentTimeMillis();
                timeIsOver = stopTime > startTime + totalPollTime;
            };

            kafkaxRun.getPostconditions().setSampleCount(totalRecords);
            kafkaxRun.getPostconditions().setSize(totalSize);

//            sb.append('\n').append("stopTime:").append(stopTime)
//                    .append('\n').append("recordCount:").append(totalRecords)
//                    .append('\n').append("duration:").append(stopTime - startTime)
//                    .append('\n').append("enough:").append(enough)
//                    .append('\n').append("timeIsOver").append(timeIsOver)
//            ;
//            sampleResult.setResponseHeaders(sb.toString());

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

    @Override
    protected void afterRun(JavaSamplerContext context, SampleResult sampleResult, KafkaxRun run) {
        // cut this
        sampleResult.setRequestHeaders(run.getPreconditions().toString());
        sampleResult.setResponseHeaders(run.getPostconditions().toString());
        final String payload = run.getPayload().toString();
        sampleResult.setResponseData(payload, ENCODING);
        sampleResult.setSamplerData("No request data for Consumer");
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
