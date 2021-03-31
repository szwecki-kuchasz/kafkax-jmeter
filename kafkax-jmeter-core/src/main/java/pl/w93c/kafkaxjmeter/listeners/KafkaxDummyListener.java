package pl.w93c.kafkaxjmeter.listeners;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;

public class KafkaxDummyListener extends AbstractVisualizer {
    @Override
    public String getLabelResource() {
        return "Kafkax Dummy Listener";
    }

    @Override
    public void clearData() {
        // nop
    }

    @Override
    public void add(SampleResult sample) {
        System.out.println(getLabelResource() + " added sample: " + sample);
    }
}
