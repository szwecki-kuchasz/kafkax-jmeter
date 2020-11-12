package pl.w93c.kafkaxjmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import pl.w93c.kafkaxjmeter.helpers.ExceptionHelper;
import pl.w93c.kafkaxjmeter.run.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static pl.w93c.kafkaxjmeter.helpers.ParamsParser.isEmpty;

/**
 * Abstract JMeter sampler, that knows something about Kafka...
 * Specializations may produce or consume
 * Inspiration and fragments of this class from repo:
 * https://github.com/BrightTag/kafkameter
 */
public abstract class KafkaxSampler extends AbstractJavaSamplerClient {

    protected static final String BROKERS = "kafka_brokers";
    protected static final String TOPIC = "kafka_topic";
//    protected static final String VALUE_SERIALIZER = "kafka_value_serializer";
//    protected static final String KEY_SERIALIZER = "kafka_key_serializer";
    protected static final String SSL_KEYSTORE = "kafka_ssl_keystore";
    protected static final String SSL_KEYSTORE_PASSWORD = "kafka_ssl_keystore_password";
    protected static final String SSL_TRUSTSTORE = "kafka_ssl_truststore";
    protected static final String SSL_TRUSTSTORE_PASSWORD = "kafka_ssl_truststore_password";
    protected static final String USE_SSL = "kafka_use_ssl";
    protected static final String COMPRESSION_TYPE = "kafka_compression_type";
    protected static final String MOCK = "kafka_mock";
    protected static final String REPORT_STACKTRACE = "kafka_report_stacktrace";
    protected static final String EMPTY_VALUE = "";
    protected static final String ENCODING = "UTF-8";
    protected static final String NOT_SET_YET = "value not set yet";
    protected boolean reportStacktrace;

    private boolean mock;
    private String bootstrap;
    private boolean ssl;

    protected final boolean isMock() {
        return mock;
    }

    private final Map<String, String> paramsMap = initParams();

    private Map<String, String> initParams() {
        Map<String, String> map = new HashMap<>();
        populateParams(map);
        return map;
    }

    protected final Map<String, String> getParamsMap() {
        return paramsMap;
    }

    protected void populateParams(final Map<String, String> map) {
        map.put(MOCK, "false");
        map.put(BROKERS, "${PARAMETER_KAFKA_BROKERS}");
        map.put(TOPIC, "${PARAMETER_KAFKA_TOPIC}");
        map.put(SSL_KEYSTORE, "${PARAMETER_KAFKA_SSL_KEYSTORE}");
        map.put(SSL_KEYSTORE_PASSWORD, "${PARAMETER_KAFKA_SSL_KEYSTORE_PASSWORD}");
        map.put(SSL_TRUSTSTORE, "${PARAMETER_KAFKA_SSL_TRUSTSTORE}");
        map.put(SSL_TRUSTSTORE_PASSWORD, "${PARAMETER_KAFKA_SSL_TRUSTSTORE_PASSWORD}");
        map.put(USE_SSL, "${PARAMETER_KAFKA_USE_SSL}");
        map.put(COMPRESSION_TYPE, null);
        map.put(REPORT_STACKTRACE, "false");
    }

    @Override
    public final Arguments getDefaultParameters() {
        final Arguments defaultParameters = new Arguments();
        // https://mkyong.com/java8/java-8-how-to-sort-a-map/
        getParamsMap().entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).forEach(e -> defaultParameters.addArgument(e.getKey(), e.getValue()));
        return defaultParameters;
    }

    protected final String getParam(JavaSamplerContext context, String name) {
        return context.getParameter(name);
    }

    private final Properties props = new Properties();

    protected final Properties getProps() {
        return props;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {
        bootstrap = context.getParameter(BROKERS);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put(ProducerConfig.ACKS_CONFIG, "1");

        // check if kafka security protocol is SSL or PLAINTEXT (default)
        ssl = Boolean.TRUE.toString().equalsIgnoreCase(context.getParameter(USE_SSL));
        if (ssl) {
            props.put("security.protocol", "SSL");
            props.put("ssl.keystore.location", context.getParameter(SSL_KEYSTORE));
            props.put("ssl.keystore.password", context.getParameter(SSL_KEYSTORE_PASSWORD));
            props.put("ssl.truststore.location", context.getParameter(SSL_TRUSTSTORE));
            props.put("ssl.truststore.password", context.getParameter(SSL_TRUSTSTORE_PASSWORD));
        } else {
            props.put("security.protocol", "PLAINTEXT");
        }

        String compressionType = context.getParameter(COMPRESSION_TYPE);
        if (!isEmpty(compressionType)) {
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        }

        mock = Boolean.TRUE.toString().equalsIgnoreCase(context.getParameter(MOCK));

        reportStacktrace =
                Boolean.TRUE.toString().equalsIgnoreCase(context.getParameter(REPORT_STACKTRACE));

    }

    @Override
    public final SampleResult runTest(JavaSamplerContext context) {
        KafkaxRun run = createRun();
        SampleResult sampleResult = new SampleResult();
        beforeRun(context, sampleResult, run);
        sampleResult.sampleStart();
        try {
            runTestImpl(context, run);
            afterSuccess(sampleResult);
        } catch (Exception e) {
            afterFail(sampleResult, ExceptionHelper.getStackTrace(e), e);
        }
        afterRun(context, sampleResult, run);
        return sampleResult;
    }

    protected void beforeRun(JavaSamplerContext context, SampleResult result, KafkaxRun run) {
        result.setDataEncoding(ENCODING);
        result.setDataType(SampleResult.TEXT);
    }

    protected abstract void runTestImpl(JavaSamplerContext context, KafkaxRun kafkaxRun) throws Exception;

    private void afterSuccess(SampleResult result) {
        result.sampleEnd();
        result.setSuccessful(true);
        result.setResponseCodeOK();
    }

    private void afterFail(SampleResult result, String reason, Exception exception) {
        result.sampleEnd();
        result.setSuccessful(false);
        result.setResponseCode(reason);
        result.setResponseMessage("Exception: " + exception);
        result.setResponseData(ExceptionHelper.getStackTrace(exception), ENCODING);
    }

    @SuppressWarnings("unused")
    protected void afterRun(JavaSamplerContext context, SampleResult sampleResult, KafkaxRun run) {
        run.getKafkaParameters().setEndTime(System.currentTimeMillis());
        sampleResult.setRequestHeaders(run.toString());
        sampleResult.setResponseHeaders("See: Request headers");
        sampleResult.setSentBytes(run.getPostconditions().getSize());
        sampleResult.setResponseData("See: Request body", ENCODING);
        sampleResult.setSamplerData(run.getPayload().toString());
    }

    private KafkaxRun createRun() {
        return KafkaxRun.newBuilder()
                .setKafkaParameters(KafkaParameters.newBuilder()
                        .setBrokers(bootstrap)
                        .setTopic(NOT_SET_YET)
                        .setMock(mock)
                        .setStartTime(System.currentTimeMillis())
                        .setEndTime(null)
                        .setSsl(ssl)
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
    }

    protected void addResult(KafkaxRun run, int counter, Long offset, CharSequence key, CharSequence value, byte[] rawValue, long timestamp) {
        try {
            run.getPayload().add(
                    KafkaxPayload.newBuilder()
                            .setCounter(counter)
                            .setKey(key)
                            .setValue(value)
                            .setRawValue(rawValue != null
                                    ? ByteBuffer.wrap(rawValue)
                                    : null)
                            .setOffset(offset)
                            .setTimestamp(timestamp)
                            .build()
            );
        } catch (Exception surprise) {
            getNewLogger().warn("Unexpected exception:", surprise);
        }
    }

    protected void addError(KafkaxRun run, int counter, Long offset, Exception e, byte[] rawValue) {
        try {
            final String stack =
                    reportStacktrace ? ExceptionHelper.getStackTrace(e)
                            : "no details due to " + REPORT_STACKTRACE + " = false";
            run.getErrors()
                    .add(
                            KafkaxFail.newBuilder()
                                    .setCounter(counter)
                                    .setRawValue(rawValue != null
                                            ? ByteBuffer.wrap(rawValue)
                                            : null)
                                    .setOffset(offset)
                                    .setException(e.getClass().getCanonicalName())
                                    .setExceptionMessage(e.getMessage())
                                    .setExceptionStackTrace(stack)
                                    .build()
                    );
        } catch (Exception surprise) {
            getNewLogger().warn("Unexpected exception:", surprise);
        }
    }

}
