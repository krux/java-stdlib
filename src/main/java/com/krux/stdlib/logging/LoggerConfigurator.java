package com.krux.stdlib.logging;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;

public class LoggerConfigurator {
    
    private static Map<String,Level> logLevels = new HashMap<String,Level>();
    
    static {
        logLevels.put( "WARNING", Level.WARN );
        logLevels.put( "DEBUG", Level.DEBUG );
        logLevels.put( "ERROR", Level.ERROR );
        logLevels.put( "CRITICAL", Level.FATAL );
        logLevels.put( "INFO", Level.INFO );
    }
    
    public static void configureLogging( String baseLoggingDir, String loglevel, String appName ) {
        
        if ( !baseLoggingDir.endsWith( "/" ) ) {
            baseLoggingDir = baseLoggingDir + "/";
        }
        //set a system property so other loggers write the correct place
        System.setProperty( "base-app-log-dir", baseLoggingDir + appName );
        
        //wrap stdout & stderr in log4j appenders (will still also write to stdout/err)
        StdOutErrLog.tieSystemOutAndErrToLog();
        
    }

}
