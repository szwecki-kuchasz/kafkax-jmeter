package pl.w93c.kafkaxjmeter.examples;

import pl.w93c.kafkaxjmeter.consumers.KafkaxSpecificAvroConsumer;
import pl.w93c.kafkaxjmeter.examples.weather.Weather;

public class WeatherKafkaxConsumer extends KafkaxSpecificAvroConsumer<Weather> {

    @Override
    protected Class<Weather> getParameterClass() {
        return Weather.class;
    }

    @Override
    protected void processRecord(Weather weather) {
        System.out.println("weather record read from topic: " + weather);
    }

}
