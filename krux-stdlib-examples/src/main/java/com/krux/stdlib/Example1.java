package com.krux.stdlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import joptsimple.OptionSet;

import com.krux.stdlib.KruxStdLib;

class Example1 {
    private static final Logger LOGGER = LoggerFactory.getLogger(Example1.class);

    private static OptionSet options;
    private static KruxStdLib stdlib = KruxStdLib.get();

    public static void main(String[] args) {
        options = stdlib.parseAndInitialize(args);

        // You can control the log threshold level with the
        // environment variable KRUX_LOGGER_LEVEL.
        LOGGER.debug("Log at DEBUG level");
        LOGGER.info("Log at INFO level");
        LOGGER.warn("Log at WARN level");
        LOGGER.error("Log at ERROR level");
        LOGGER.info("options = {}", options);
    }
}
