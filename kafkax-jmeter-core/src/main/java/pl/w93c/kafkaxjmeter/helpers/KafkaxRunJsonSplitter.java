package pl.w93c.kafkaxjmeter.helpers;

import pl.w93c.kafkaxjmeter.run.KafkaxFail;
import pl.w93c.kafkaxjmeter.run.KafkaxRun;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.w93c.kafkaxjmeter.helpers.AvroToStringCorrectionHelper.correctAvroTimeStamp;

/**
 * It's hard for JMeter and other tools to process one-line JSON result of Kafkax run.
 * We need some hack, to make this result more readable.
 */
public class KafkaxRunJsonSplitter {

    public static final String printPayload(KafkaxRun kafkaxRun) {

        return new StringBuilder("\"payload\": [")
                .append(
                        kafkaxRun.getPayload().stream()
                                .map(payload -> correctAvroTimeStamp(payload.toString()))
                                .collect(Collectors.joining(",\n"))
                )
                .append("\n]")
                .toString();
    }

    public static final String printErrors(KafkaxRun kafkaxRun) {

        return new StringBuilder("\"errors\": [")
                .append(
                        kafkaxRun.getErrors().stream()
                                .map(fail -> correctAvroTimeStamp(fail.toString()))
                                .collect(Collectors.joining(",\n"))
                )
                .append("\n]")
                .toString();
    }

    public static final String printKafkaxRun(KafkaxRun kafkaxRun) {
        return
                new StringBuilder("{\n")
                        .append(
                                Stream.of(
                                        "\"kafkaParameters\": " + kafkaxRun.getKafkaParameters(),
                                        "\"preconditions\": " + kafkaxRun.getPreconditions(),
                                        printPayload(kafkaxRun),
                                        printErrors(kafkaxRun),
                                        "\"postconditions\": " + kafkaxRun.getPostconditions()
                                )
                                        .map(element -> correctAvroTimeStamp(element.toString()))
                                        .collect(Collectors.joining(",\n")))
                        .append("\n}")
                        .toString();
    }

}
