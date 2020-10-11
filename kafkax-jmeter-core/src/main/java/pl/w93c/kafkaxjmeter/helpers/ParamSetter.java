package pl.w93c.kafkaxjmeter.helpers;

import org.apache.jmeter.config.Arguments;

public class ParamSetter {

    public static void setArgument(Arguments args, String name, String value) {
        for (int i = 0; i < args.getArgumentCount(); i++) {
            if (name.equals(args.getArgument(i).getName())) {
                args.getArgument(i).setValue(value);
                return;
            }
        }
        args.addArgument(name, value);
    }

}
