/**
 * 
 */
package com.krux.stdlib;

import java.io.IOException;
import java.net.InetAddress;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ubercraft.statsd.StatsdClient;

import com.krux.stdlib.logging.LoggerConfigurator;
import com.krux.stdlib.statsd.NoopStatsdClient;

/**
 * @author casspc
 *
 */
public class KruxStdLib {

	final static Logger logger = (Logger)LoggerFactory.getLogger( KruxStdLib.class );
	
	/**
	 * See 
	 */
	public static StatsdClient statsd = null;
	public static String env;
	public static String appName;
	
	private static OptionParser _parser = null;
	private static OptionSet _options = null;
	private static boolean _initialized = false;
	
	/**
	 * If you'd like to utilize the std lib parser for your app-specific cli argument parsing needs,
	 * feel free to pass a configured OptionParser to this class before initializing it.  
	 * (see this class' source for examples and http://pholser.github.io/jopt-simple/examples.html 
	 * for details on how to create and configure the OptionParser)
	 * @param parser a Configured OptionParser
	 */
	public static void setOptionParser( OptionParser parser ) {
		_parser = parser;
	}
	
	
	/** 
	 * Initializes the std libary static functions, including logging, statsd client and a
	 * "status" http listener
	 * @param args The command line args that were passed to your main( String[] args ) 
	 * @return the OptionSet of parsed command line arguments
	 */
	public static OptionSet initialize( String[] args ) {
		
		if ( !_initialized ) {
			//parse command line, handle common needs
			
			//some defaults
			final String defaultStatsdHost = "localhost";
			final int defaultStatsdPort = 8125;
			final String defaultEnv = "dev";
			final String defaultLogLevel = "DEBUG";
			final Boolean defaultUseStatsd = false;
			final String defaultAppName = getMainClassName();
			final String baseApplicationLoggingDir = "/data/var/log/";
			
			OptionParser parser;
			if ( _parser == null ) {
				parser = new OptionParser();
			} else {
				parser = _parser;
			}
			
			parser.accepts( "help", "Prints this helpful message" );
			OptionSpec<Boolean> enableStatsd = 
					parser.accepts( "stats", "Enable/disable statsd broadcast" )
						.withOptionalArg().ofType(Boolean.class).defaultsTo( defaultUseStatsd );
			OptionSpec<String> statsdHost = 
					parser.accepts( "stats-host", "Listening statsd host" )
						.withOptionalArg().ofType(String.class).defaultsTo( defaultStatsdHost );
			OptionSpec<Integer> statsdPort = 
					parser.accepts( "stats-port", "Listening statsd port" )
						.withOptionalArg().ofType(Integer.class).defaultsTo( defaultStatsdPort );
			OptionSpec<String> environment = 
					parser.accepts( "env", "Operating environment" )
						.withOptionalArg().ofType(String.class).defaultsTo( defaultEnv );
			OptionSpec<String> logLevel = 
					parser.accepts( "log-level", "Default log4j log level" )
						.withOptionalArg().ofType(String.class).defaultsTo( defaultLogLevel );
	         OptionSpec<String> appNameOption = 
	                    parser.accepts( "app-name", "Application identifier, used for statsd namespaces, log file names, etc. "
	                            + "If not supplied, will use this app's entry point classname." )
	                        .withOptionalArg().ofType(String.class).defaultsTo( defaultAppName );
	         OptionSpec<String> baseLoggingDir = 
                       parser.accepts( "base-logging-dir", "Base directory for application logging" )
                           .withOptionalArg().ofType(String.class).defaultsTo( baseApplicationLoggingDir );
			
			_options = parser.parse( args );
			
			//if "--help" was passed in, show some helpful guidelines and exit
			if ( _options.has( "help" ) ) {
				try {
					parser.printHelpOn( System.out );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					System.exit( 0 );
				}
			}
			
			//set environment
			env = _options.valueOf(environment);
			
	         //set global app name
            appName = _options.valueOf( appNameOption );
			
			//setup logging level
            LoggerConfigurator.configureLogging(_options.valueOf(baseLoggingDir), 
                    _options.valueOf(logLevel), appName );
			
			//setup statsd
			try {
				if ( _options.valueOf(enableStatsd) ) {
					statsd = new StatsdClient( _options.valueOf( statsdHost ), 
							_options.valueOf( statsdPort ), logger );
				} else {
					statsd = new NoopStatsdClient( InetAddress.getLocalHost(), 0);
				}
			} catch ( Exception e ) {
				logger.warn( "Cannot establish a statsd connection", e );
			}
			
			_initialized = true;
		}
		return _options;
	}
	
	private static String getMainClassName() {
	    
	    StackTraceElement[] stack = Thread.currentThread().getStackTrace ();
	    StackTraceElement main = stack[stack.length - 1];
	    String mainClass = main.getClassName();
	    if ( mainClass.contains(".") ) {
	        String[] parts = mainClass.split( "\\." );
	        mainClass = parts[ parts.length - 1 ];
	    }
	    return mainClass;
	}

}
