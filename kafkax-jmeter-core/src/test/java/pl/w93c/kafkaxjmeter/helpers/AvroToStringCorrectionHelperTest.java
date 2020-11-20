package pl.w93c.kafkaxjmeter.helpers;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AvroToStringCorrectionHelperTest {

    private static final String INPUT = "\"startTime\": 2020-11-19T20:47:40.281+01:00, \"endTime\": 2020-11-19T20:48:05.351+01:00}, \"preconditions\": {";

    // "startTime": "2020-11-19T20:47:40.281+01:00", "endTime": "2020-11-19T20:48:05.351+01:00"}, "preconditions": {
    private static final String EXPECTED = "\"startTime\": \"2020-11-19T20:47:40.281+01:00\", \"endTime\": \"2020-11-19T20:48:05.351+01:00\"}, \"preconditions\": {";

    @Test
    public void correctAvroTimeStamp() {
        final String output = AvroToStringCorrectionHelper.correctAvroTimeStamp(INPUT);
        assertThat(output)
            .contains(EXPECTED);
    }
}