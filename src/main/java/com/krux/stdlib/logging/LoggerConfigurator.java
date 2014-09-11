package com.krux.stdlib.logging;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.AsyncAppender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class LoggerConfigurator {

    private static Map<String, Level> logLevels = new HashMap<String, Level>();

    static {
        logLevels.put( "WARN", Level.WARN );
        logLevels.put( "DEBUG", Level.DEBUG );
        logLevels.put( "ERROR", Level.ERROR );
        logLevels.put( "FATAL", Level.FATAL );
        logLevels.put( "INFO", Level.INFO );
    }

    public static void configureLogging( String baseLoggingDir, String loglevel, String appName ) {

        if ( !baseLoggingDir.endsWith( "/" ) ) {
            baseLoggingDir = baseLoggingDir + "/";
        }

        String baseAppLoggingDir = baseLoggingDir + appName + "/";
        // set a system property so other loggers write the correct place
        System.setProperty( "base-app-log-dir", baseAppLoggingDir );

        // This is the root logger provided by log4j
        Logger rootLogger = Logger.getRootLogger();

        // set default root level
        Level defaultLevel = logLevels.get( loglevel.toUpperCase() );
        if ( defaultLevel == null ) {
            System.err
                    .println( "--log-level is not a valid value! Please pass WARN, DEBUG, ERROR, FATAL or INFO. Defaulting to WARN. "
                            + loglevel );
            defaultLevel = Level.WARN;
        }
        rootLogger.setLevel( defaultLevel );

        // Define log pattern layout
        PatternLayout layout = new PatternLayout( "%d{ISO8601} %-6p: [%t] %c{2} %x - %m%n" );

        try {
            // DOH! nvm...ops would like us to log to console unless an app
            // has a specific requirement not to
            ConsoleAppender consoleAppender = new ConsoleAppender();
            consoleAppender.setLayout( layout );
            consoleAppender.setName( "stdlib-console-out" );
            consoleAppender.setWriter( new OutputStreamWriter( System.out ) );

            ConsoleAppender errorAppender = new ConsoleAppender();
            errorAppender.setLayout( layout );
            errorAppender.setName( "stdlib-console-err" );
            errorAppender.setWriter( new OutputStreamWriter( System.err ) );
            errorAppender.setThreshold( Level.WARN );

            // Wrap the console appenders in an async appenders
            AsyncAppender asyncOut = new AsyncAppender();
            asyncOut.setBlocking( true );
            asyncOut.setBufferSize( 2048 );
            asyncOut.addAppender( consoleAppender );
            asyncOut.addAppender( errorAppender );
            asyncOut.setName( "stdlib-async-out" );

            // Add the appender to root logger
            rootLogger.addAppender( asyncOut );

        } catch ( Exception e ) {
            System.out.println( "Failed to add appender!!" );
            e.printStackTrace();
        }

        // wrap stdout & stderr in log4j appenders (will still also write to
        // stdout/err)
        StdOutErrLog.tieSystemOutAndErrToLog();

    }

}
