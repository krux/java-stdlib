package com.krux.stdlib;

import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

class Example1 {
    private static KruxStdLib stdlib = new KruxStdLib();
    private static final Logger LOGGER = LoggerFactory.getLogger(Example1.class);
    private static final String APP_DESCRIPTON = "Krux stdlib Example1 - Echos options and logs at different levels";

    public static void main(String[] args) {
        OptionSet opt = stdlib.parseAndInitialize(APP_DESCRIPTON, args);

        List<String> basicOptions = Arrays.asList(
            "app-name",
            "base-dir",
            "env",
            "http-port",
            "property-file",
            "stats-environment"
        );

        System.out.println("Options: (not exhaustive)");
        System.out.println("-------------------------");
        basicOptions.forEach(optionName -> {
            if (opt.has(optionName)) {
                System.out.println("--" + optionName + "=" + opt.valueOf(optionName));
            }
        });

        System.out.println("Starting...");

        // You can control the log threshold level with the
        // environment variable KRUX_LOGGER_LEVEL.
        LOGGER.debug("Log at DEBUG level");
        LOGGER.info("Log at INFO level");
        LOGGER.warn("Log at WARN level");
        LOGGER.error("Log at ERROR level");
    }
}
