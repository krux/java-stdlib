package com.krux.stdlib.logging;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;

public class LoggerConfigurator {

    private static Map<String, Level> logLevels = new HashMap<String, Level>();

    static {
        logLevels.put( "WARN", Level.WARN );
        logLevels.put( "DEBUG", Level.DEBUG );
        logLevels.put( "ERROR", Level.ERROR );
        logLevels.put( "INFO", Level.INFO );
    }

    public static void configureRotatingLogging( String baseLoggingDir, String loglevel, String appName ) {
        
        if ( !baseLoggingDir.endsWith( "/" ) ) {
            baseLoggingDir = baseLoggingDir + "/";
        }
        
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();
        Logger rootLogger = getRootLogger( loglevel );
        PatternLayoutEncoder ple = getPatternLayoutEncoder( lc );
        
        String baseAppLoggingDir = baseLoggingDir;
        // set a system property so other loggers write the correct place
        System.setProperty( "krux-base-app-log-dir", baseAppLoggingDir );

        try {
            // Define file appender with layout and output log file name
            RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<ILoggingEvent>();
            fileAppender.setContext( lc );
            fileAppender.setName( "krux-file-appender" );
            fileAppender.setFile( baseAppLoggingDir + appName + ".log" );
            fileAppender.setAppend( true );
            fileAppender.setEncoder( ple );
            
            FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
            rollingPolicy.setContext( lc );
            rollingPolicy.setMinIndex( 1 );
            rollingPolicy.setMaxIndex( 9 );
            rollingPolicy.setFileNamePattern( baseAppLoggingDir + appName + ".%i.log.gz" );
            rollingPolicy.setParent( fileAppender );
            rollingPolicy.start();
            
            SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>( "50MB" );
            triggeringPolicy.setContext( lc );
            triggeringPolicy.start();

            fileAppender.setRollingPolicy( rollingPolicy );
            fileAppender.setTriggeringPolicy( triggeringPolicy );
            fileAppender.start();

            // Wrap the console appenders in an async appenders
            AsyncAppender asyncOut = new AsyncAppender();
            asyncOut.setContext( lc );
            asyncOut.setDiscardingThreshold( 0 );
            asyncOut.setQueueSize( 500 );
            asyncOut.addAppender( fileAppender );
            asyncOut.setName( "stdlib-async-out" );
            asyncOut.start();

            // Add the appender to root logger
            rootLogger.addAppender( asyncOut );
            
            // wrap stdout & stderr in log4j appenders 
            StdOutErrLog.tieSystemOutAndErrToLog();
            
        } catch ( Exception e ) {
            System.out.println( "Failed to add appender !!" );
            e.printStackTrace();
        }

    }

    private static PatternLayoutEncoder getPatternLayoutEncoder( LoggerContext lc ) {
        PatternLayoutEncoder ple = new PatternLayoutEncoder();
        ple.setPattern("%-12date{YYYY-MM-dd'T'HH:mm:ss.SSS} |%-5level [%thread] %logger{15} - %msg%n");
        ple.setContext(lc);
        ple.start();
        return ple;
    }

    public static void configureStdOutLogging( String loglevel ) {

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();
        Logger rootLogger = getRootLogger( loglevel );
        PatternLayoutEncoder ple = getPatternLayoutEncoder( lc );

        try {
            // ops would like us to log to console unless an app
            // has a specific need not to
            ConsoleAppender<ILoggingEvent> stdOutAppender = new ConsoleAppender<ILoggingEvent>();
            stdOutAppender.setContext( lc );
            stdOutAppender.setName( "stdlib-console-out" );
            stdOutAppender.setTarget( "System.out" );
            stdOutAppender.setEncoder( ple );
            
            Filter<ILoggingEvent> stdOutFilter = new StdOutFilter();
            stdOutAppender.addFilter( stdOutFilter );
            stdOutFilter.start();
            stdOutAppender.start();

            ple = getPatternLayoutEncoder( lc );
            
            ConsoleAppender<ILoggingEvent> errorAppender = new ConsoleAppender<ILoggingEvent>();
            errorAppender.setContext( lc );
            errorAppender.setName( "stdlib-console-err" );
            errorAppender.setTarget( "System.err" );
            errorAppender.setEncoder( ple );
            
            Filter<ILoggingEvent> stdErrFilter = new ErrOutFilter();
            errorAppender.addFilter( stdErrFilter );
            stdErrFilter.start();
            errorAppender.start();

            // Wrap the console appenders in an async appenders
            AsyncAppender asyncStdOutWrapper = new AsyncAppender();
            asyncStdOutWrapper.setContext( lc );
            asyncStdOutWrapper.setDiscardingThreshold( 0 );
            asyncStdOutWrapper.setQueueSize( 500 );
            asyncStdOutWrapper.addAppender( stdOutAppender );
            asyncStdOutWrapper.setName( "stdlib-async-out" );
            asyncStdOutWrapper.start();
            
            // Wrap the console appenders in an async appenders
            AsyncAppender asyncStdErrWrapper = new AsyncAppender();
            asyncStdErrWrapper.setContext( lc );
            asyncStdErrWrapper.setDiscardingThreshold( 0 );
            asyncStdErrWrapper.setQueueSize( 500 );
            asyncStdErrWrapper.addAppender( errorAppender ); 
            asyncStdErrWrapper.setName( "stdlib-async-err-2" );
            asyncStdErrWrapper.start();

            // Add the appenders to root logger
            rootLogger.addAppender( asyncStdOutWrapper );
            rootLogger.addAppender( asyncStdErrWrapper );

        } catch ( Exception e ) {
            System.out.println( "Failed to add appender!!" );
            e.printStackTrace();
        }

        // wrap stdout & stderr in log4j appenders 
        StdOutErrLog.tieSystemOutAndErrToLog();

    }

    private static Logger getRootLogger( String loglevel ) {
        Logger rootLogger = (Logger) LoggerFactory.getLogger( Logger.ROOT_LOGGER_NAME );
        setLogLevel( loglevel, rootLogger );
        rootLogger.setAdditive(false);
        return rootLogger;
    }

    private static void setLogLevel( String loglevel, Logger rootLogger ) {
        // set default root level
        Level defaultLevel = logLevels.get( loglevel.toUpperCase() );
        if ( defaultLevel == null ) {
            System.err
                    .println( "--log-level is not a valid value! Please pass WARN, DEBUG, ERROR, or INFO. Defaulting to WARN. "
                            + loglevel );
            defaultLevel = Level.WARN;
        }
        rootLogger.setLevel( defaultLevel );
    }

}
