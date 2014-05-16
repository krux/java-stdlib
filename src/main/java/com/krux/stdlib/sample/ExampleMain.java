package com.krux.stdlib.sample;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;

/**
 * A sample program leverages the KruxStdLib for logging, cli, statsd and http status functionality.
 * Pass the cl switch "--help" for usage guidelines
 * 
 * @author casspc
 * 
 */
public class ExampleMain {
    
    final static Logger logger = LoggerFactory.getLogger(ExampleMain.class);

	public static void main(String[] args) {
		
		//The KruxStdLib handles parsing of a set of reserved command line options common across
		//our python and java codebases.  See KruxStdLib.initialize() for details.
		
	    //The KruxStdLib will also handle parsing of app-specific command-line options if you create
	    //and pass an OptionParser to it *before* calling its initialize() method.  They will 
	    //be added to the reserved options list (and even show up in the helpful --help output)
	    
		//handle a couple custom cli-params
	    OptionParser parser = new OptionParser();
        OptionSpec<String> optionalOption = 
                parser.accepts( "optional-option", "An optional custom option" )
                    .withOptionalArg().ofType(String.class).defaultsTo( "value1" );
        
        OptionSpec<String> requiredOption = 
                parser.accepts( "required-option", "A required custom option" )
                    .withRequiredArg().ofType(String.class).defaultsTo( "value2" );
	    
        //give parser to KruxStdLib so it can add our params to the reserved list
        KruxStdLib.setOptionParser(parser);
        
        //Initializing the KruxStdLib will does a few things:
        //	1. The command line args passed to initialize() are parsed and validated.  Values for cl options 
        //		that have parameter values are available in the returned OptionSet object
        //	2. An asynchronous log4j appender that wraps a DailyRollingFileAppender is programatically created
        //		and added to the rootlogger. See LoggerConfigurator for details.
        //	3. A static statsd client library is instantiated and made avaialble as a static member of KruxStdLib
        //	4. An HTTP server is started that will respond to "/__status" requests with a 200 OK. 
		OptionSet options = KruxStdLib.initialize(args);
		
		logger.info( optionalOption.toString() + ": " + options.valueOf( optionalOption ) );
		logger.info( requiredOption.toString() + ": " + options.valueOf( requiredOption ) );	
		logger.debug( "This was logged using the stdlib logging configuration" );
		logger.warn( "This was logged using the stdlib logging configuration" );
		logger.error( "This was logged using the stdlib logging configuration" );
		
		try {
		    throw new Exception( "An exception was thrown" );
		} catch ( Exception e ) {
		    logger.error( "Uh oh.", e );
		}
		System.out.println( "This is printed to standard out" );

	}

}
