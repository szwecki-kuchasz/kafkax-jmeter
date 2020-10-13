package pl.w93c.kafkaxjmeter;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.io.PrintWriter;
import java.io.StringWriter;
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
    /**
     * Parameter for setting the Kafka brokers; for example, "kafka01:9092,kafka02:9092".
     */
    protected static final String PARAMETER_KAFKA_BROKERS = "kafka_brokers";
    /**
     * Parameter for setting the Kafka topic name.
     */
    protected static final String PARAMETER_KAFKA_TOPIC = "kafka_topic";
    /**
     * Parameter for setting the Kafka key.
     */
    protected static final String PARAMETER_KAFKA_KEY = "kafka_key";
    /**
     * Parameter for setting Kafka's {@code serializer.class} property.
     */
//    protected static final String PARAMETER_KAFKA_MESSAGE_SERIALIZER = "kafka_message_serializer";
    /**
     * Parameter for setting Kafka's {@code key.serializer.class} property.
     */
//    protected static final String PARAMETER_KAFKA_KEY_SERIALIZER = "kafka_key_serializer";
    /**
     * Parameter for setting the Kafka ssl keystore (include path information); for example, "server.keystore.jks".
     */
    protected static final String PARAMETER_KAFKA_SSL_KEYSTORE = "kafka_ssl_keystore";
    /**
     * Parameter for setting the Kafka ssl keystore password.
     */
    protected static final String PARAMETER_KAFKA_SSL_KEYSTORE_PASSWORD = "kafka_ssl_keystore_password";
    /**
     * Parameter for setting the Kafka ssl truststore (include path information); for example, "client.truststore.jks".
     */
    protected static final String PARAMETER_KAFKA_SSL_TRUSTSTORE = "kafka_ssl_truststore";
    /**
     * Parameter for setting the Kafka ssl truststore password.
     */
    protected static final String PARAMETER_KAFKA_SSL_TRUSTSTORE_PASSWORD = "kafka_ssl_truststore_password";
    /**
     * Parameter for setting the Kafka security protocol; "true" or "false".
     */
    protected static final String PARAMETER_KAFKA_USE_SSL = "kafka_use_ssl";
    /**
     * Parameter for setting encryption. It is optional.
     */
    protected static final String PARAMETER_KAFKA_COMPRESSION_TYPE = "kafka_compression_type";
    /**
     * Parameter for mocking write
     */
    protected static final String PARAMETER_KAFKA_MOCK = "kafka_mock";
    protected static final String EMPTY_VALUE = "";
    /**
     * Use UTF-8 for encoding of strings
     */
    private static final String ENCODING = "UTF-8";

    private boolean mock;

    public final boolean isMock() {
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

    protected void populateParams(final Map<String, String> map ) {
        map.put(PARAMETER_KAFKA_MOCK, "false");
        map.put(PARAMETER_KAFKA_BROKERS, "${PARAMETER_KAFKA_BROKERS}");
        map.put(PARAMETER_KAFKA_TOPIC, "${PARAMETER_KAFKA_TOPIC}");
        map.put(PARAMETER_KAFKA_KEY, "${PARAMETER_KAFKA_KEY}");
        map.put(PARAMETER_KAFKA_SSL_KEYSTORE, "${PARAMETER_KAFKA_SSL_KEYSTORE}");
        map.put(PARAMETER_KAFKA_SSL_KEYSTORE_PASSWORD, "${PARAMETER_KAFKA_SSL_KEYSTORE_PASSWORD}");
        map.put(PARAMETER_KAFKA_SSL_TRUSTSTORE, "${PARAMETER_KAFKA_SSL_TRUSTSTORE}");
        map.put(PARAMETER_KAFKA_SSL_TRUSTSTORE_PASSWORD, "${PARAMETER_KAFKA_SSL_TRUSTSTORE_PASSWORD}");
        map.put(PARAMETER_KAFKA_USE_SSL, "${PARAMETER_KAFKA_USE_SSL}");
        map.put(PARAMETER_KAFKA_COMPRESSION_TYPE, null);
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

    private Properties props = new Properties();

    protected final Properties getProps() {
        return props;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, context.getParameter(PARAMETER_KAFKA_BROKERS));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put(ProducerConfig.ACKS_CONFIG, "1");

        // check if kafka security protocol is SSL or PLAINTEXT (default)
        if (Boolean.TRUE.toString().equalsIgnoreCase(context.getParameter(PARAMETER_KAFKA_USE_SSL))) {
            props.put("security.protocol", "SSL");
            props.put("ssl.keystore.location", context.getParameter(PARAMETER_KAFKA_SSL_KEYSTORE));
            props.put("ssl.keystore.password", context.getParameter(PARAMETER_KAFKA_SSL_KEYSTORE_PASSWORD));
            props.put("ssl.truststore.location", context.getParameter(PARAMETER_KAFKA_SSL_TRUSTSTORE));
            props.put("ssl.truststore.password", context.getParameter(PARAMETER_KAFKA_SSL_TRUSTSTORE_PASSWORD));
        } else {
            props.put("security.protocol", "PLAINTEXT");
        }

        String compressionType = context.getParameter(PARAMETER_KAFKA_COMPRESSION_TYPE);
        if (!isEmpty(compressionType)) {
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        }

        mock = Boolean.TRUE.toString().equalsIgnoreCase(context.getParameter(PARAMETER_KAFKA_MOCK));

    }

    protected abstract String getResultData(JavaSamplerContext context);

    @Override
    public final SampleResult runTest(JavaSamplerContext context) {
        SampleResult sampleResult = newSampleResult();
        sampleResultStart(sampleResult, getResultData(context));

        try {
            runTestImpl(context, sampleResult);
            sampleResultSuccess(sampleResult); // , null);
        } catch (Exception e) {
            // https://www.baeldung.com/java-stacktrace-to-string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sampleResultFailed(sampleResult, sw.toString(), e);
        }
        return sampleResult;
    }

    protected abstract void runTestImpl(JavaSamplerContext context, SampleResult result) throws Exception;

    /**
     * Factory for creating new {@link SampleResult}s.
     */
    private SampleResult newSampleResult() {
        SampleResult result = new SampleResult();
        result.setDataEncoding(ENCODING);
        result.setDataType(SampleResult.TEXT);
        return result;
    }

    /**
     * Start the sample request and set the {@code samplerData} to {@code data}.
     *
     * @param result the sample result to update
     * @param data   the request to set as {@code samplerData}
     */
    private void sampleResultStart(SampleResult result, String data) {
        result.setSamplerData(data);
        result.sampleStart();
    }

    /**
     * Mark the sample result as {@code end}ed and {@code successful} with an "OK" {@code responseCode},
     * and if the response is not {@code null} then set the {@code responseData} to {@code response},
     * otherwise it is marked as not requiring a response.
     *
     * @param result   sample result to change
//     * @param response the successful result message, may be null.
     */
    private void sampleResultSuccess(SampleResult result) {
        result.sampleEnd();
        result.setSuccessful(true);
        result.setResponseCodeOK();
//        if (response != null) {
//            result.setResponseData(response, ENCODING);
//        } else {
//            result.setResponseData("No response required", ENCODING);
//        }
    }

    /**
     * Mark the sample result as @{code end}ed and not {@code successful}, and set the
     * {@code responseCode} to {@code reason}.
     *
     * @param result the sample result to change
     * @param reason the failure reason
     */
    private void sampleResultFailed(SampleResult result, String reason) {
        result.sampleEnd();
        result.setSuccessful(false);
        result.setResponseCode(reason);
    }

    /**
     * Mark the sample result as @{code end}ed and not {@code successful}, set the
     * {@code responseCode} to {@code reason}, and set {@code responseData} to the stack trace.
     *
     * @param result    the sample result to change
     * @param exception the failure exception
     */
    private void sampleResultFailed(SampleResult result, String reason, Exception exception) {
        sampleResultFailed(result, reason);
        result.setResponseMessage("Exception: " + exception);
        result.setResponseData(getStackTrace(exception), ENCODING);
    }

    /**
     * Return the stack trace as a string.
     *
     * @param exception the exception containing the stack trace
     * @return the stack trace
     */
    private String getStackTrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
