package pl.w93c.kafkaxjmeter.examples;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import pl.w93c.kafkaxjmeter.examples.weather.PressureUnit;
import pl.w93c.kafkaxjmeter.examples.weather.TemperatureUnit;
import pl.w93c.kafkaxjmeter.examples.weather.Weather;
import pl.w93c.kafkaxjmeter.producers.KafkaxSpecificAvroProducer;

import java.util.Map;

import static pl.w93c.kafkaxjmeter.helpers.ParamsParser.toInt;

public class WeatherKafkaxProducer extends KafkaxSpecificAvroProducer<Weather> {

    @Override
    protected Weather createSpecificRecord(JavaSamplerContext context){
        return Weather.newBuilder()
                .setPlace(getParam(context, PLACE))
                .setTemperature(toInt(getParam(context, TEMPERATURE)))
                .setTemperatureUnit(TemperatureUnit.valueOf(getParam(context, TEMPERATURE_UNIT)))
                .setPressure(toInt(getParam(context, PRESSURE)))
                .setPressureUnit(PressureUnit.valueOf(getParam(context, PRESSURE_UNIT)))
                .setTime(System.currentTimeMillis())
                .build();
    }

    public static final String PLACE = "weather.place";
    public static final String TEMPERATURE = "weather.temperature";
    public static final String PRESSURE = "weather.pressure";
    public static final String TEMPERATURE_UNIT = "weather.temperature.unit [C/F]";
    public static final String PRESSURE_UNIT = "weather.pressure.unit [mmHg/hPa]";

    @Override
    protected void populateParams(Map<String, String> map) {
        super.populateParams(map);
        map.put(PLACE, EMPTY_VALUE);
        map.put(TEMPERATURE, EMPTY_VALUE);
        map.put(PRESSURE, EMPTY_VALUE);
        map.put(TEMPERATURE_UNIT, TemperatureUnit.C.name());
        map.put(PRESSURE_UNIT, PressureUnit.hPa.name());
    }

}
