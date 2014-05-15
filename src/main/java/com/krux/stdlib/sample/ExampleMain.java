package com.krux.stdlib.sample;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;

public class ExampleMain {
    
    final static Logger logger = LoggerFactory.getLogger(ExampleMain.class);

	public static void main(String[] args) {
		
	    //The KruxStdLib will handle parsing of app-specific command-line options if you create
	    //and pass an OptionParser to it *before* calling the initialize() method.  They will 
	    //be added to the default options list (and even show up in the helpful --help output)
	    
	    OptionParser parser = new OptionParser();
        OptionSpec<String> optionalOption = 
                parser.accepts( "optional-option", "An optional custom option" )
                    .withOptionalArg().ofType(String.class).defaultsTo( "value1" );
        
        OptionSpec<String> requiredOption = 
                parser.accepts( "required-option", "A required custom option" )
                    .withRequiredArg().ofType(String.class).defaultsTo( "value2" );
	    
        KruxStdLib.setOptionParser(parser);
        
        //once you've initialized KruxStdLib, you can get the values of the passed options
        //from the returned OptionSet object
		OptionSet options = KruxStdLib.initialize(args);
		
		logger.info( optionalOption.toString() + ": " + options.valueOf( optionalOption ) );
		logger.info( requiredOption.toString() + ": " + options.valueOf( requiredOption ) );
				
		logger.debug( "This was logged using the stdlib logging configuration" );
		logger.info( "This was logged using the stdlib logging configuration" );
		logger.error( "This was logged using the stdlib logging configuration" );
		
		try {
		    throw new Exception( "An exception was thrown" );
		} catch ( Exception e ) {
		    logger.error( "Uh oh.", e );
		}
		System.out.println( "This is printed to standard out" );

	}

}
