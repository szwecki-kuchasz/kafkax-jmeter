package pl.w93c.kafkaxjmeter.helpers;

import org.assertj.core.data.Percentage;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import static org.junit.Assert.*;
import static pl.w93c.kafkaxjmeter.helpers.ParamsParser.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ParamsParserTest {

    private static final String FLOATING = "3.14159";
    private static final String FLOATING_COMMA = "3,14159";
    private static final String FLOATING_EXP = "8.8542e-12";
    private static final String FLOATING_MINUS = "-3.1415926";
    private static final String INTEGERRR = "3456";
    private static final String INTEGERRR_MIMUS = "-9876";
    private static final String DATEEE = "2020-11-19";
    private static final String TIMEEE = "20:01:59";
    private static final String SZTOMPEL = "2020-11-20 20:47:01";
    private static final String STRANGEEE = "MiśKolargol";
    private static final String NULLL = (String) null;

    @Test
    public void shouldToFloat() {
        float fff;
        fff = toFloat(FLOATING);
        assertThat(fff).isCloseTo(3.14159f, Percentage.withPercentage(0.0001));
        fff = toFloat(FLOATING_COMMA);
        assertThat(fff).isCloseTo(3.14159f, Percentage.withPercentage(0.0001));
        fff = toFloat(FLOATING_MINUS);
        assertThat(fff).isCloseTo(-3.14159f, Percentage.withPercentage(0.0001));
        fff = toFloat(FLOATING_EXP);
        assertThat(fff).isCloseTo(8.8542e-12f, Percentage.withPercentage(0.0001));

        fff = toFloat(NULLL);
        assertThat(fff).isZero();

        fff = toFloat(STRANGEEE);
        assertThat(fff).isZero();
    }

    @Test
    public void shouldToLong() {
        long lll;
        lll = toLong(INTEGERRR);
        assertThat(lll).isEqualTo(3456L);
        lll = toLong(INTEGERRR_MIMUS);
        assertThat(lll).isEqualTo(-9876);

        lll = toLong(NULLL);
        assertThat(lll).isZero();

        lll = toLong(STRANGEEE);
        assertThat(lll).isZero();
    }

    @Test
    public void shouldToDecimal() {
    }

    @Test
    public void shouldToDate() {
    }

    @Test
    public void shouldToTime() {
    }

    @Test
    public void shouldToDateTime() {
       LocalDateTime ldt, expected;
        ldt = toDateTime(SZTOMPEL);
        expected = new LocalDateTime(1605901621000L);
        assertThat(ldt).isEqualByComparingTo(expected);

        ldt = toDateTime(STRANGEEE);
        long now = System.currentTimeMillis();
        assertThat(ldt).isBetween(new LocalDateTime(now - 1000), new LocalDateTime(now));
    }

}