package com.krux.stdlib.logging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class LoggerConfigurator {

    private static Map<String, Level> logLevels = new HashMap<String, Level>();

    static {
        logLevels.put("WARNING", Level.WARN);
        logLevels.put("DEBUG", Level.DEBUG);
        logLevels.put("ERROR", Level.ERROR);
        logLevels.put("CRITICAL", Level.FATAL);
        logLevels.put("INFO", Level.INFO);
    }

    public static void configureLogging(String baseLoggingDir, String loglevel, String appName) {

        if (!baseLoggingDir.endsWith("/")) {
            baseLoggingDir = baseLoggingDir + "/";
        }

        String baseAppLoggingDir = baseLoggingDir + appName + "/";
        // set a system property so other loggers write the correct place
        System.setProperty("base-app-log-dir", baseAppLoggingDir);

        // This is the root logger provided by log4j
        Logger rootLogger = Logger.getRootLogger();
        
        //set default root level
        Level defaultLevel = logLevels.get(loglevel);
        if (defaultLevel == null) {
            defaultLevel = Level.DEBUG;
        }
        rootLogger.setLevel(defaultLevel);

        // Define log pattern layout
        PatternLayout layout = new PatternLayout("%d{ISO8601} %-6p : [%t] %c{2} %x - %m%n");

        try {
            // Define file appender with layout and output log file name
            String rootLoggerFile = baseAppLoggingDir + appName + ".log";
            RollingFileAppender fileAppender = new RollingFileAppender(layout, rootLoggerFile);

            // Add the appender to root logger
            rootLogger.addAppender(fileAppender);
        } catch (IOException e) {
            System.out.println("Failed to add appender !!");
        }

        // wrap stdout & stderr in log4j appenders (will still also write to
        // stdout/err)
        StdOutErrLog.tieSystemOutAndErrToLog();

    }

}
