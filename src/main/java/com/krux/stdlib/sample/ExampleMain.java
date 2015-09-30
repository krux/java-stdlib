package com.krux.stdlib.sample;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;
import com.krux.stdlib.shutdown.ShutdownTask;

/**
 * A sample program leverages the KruxStdLib for logging, cli, statsd and http
 * status functionality. Pass the cl switch "--help" for usage guidelines
 * 
 * @author casspc
 * 
 */
public class ExampleMain {

    final static Logger logger = LoggerFactory.getLogger( ExampleMain.class );

    public static void main( String[] args ) {

        // The KruxStdLib handles parsing of a set of reserved command line
        // options common across
        // our python and java codebases. See KruxStdLib.initialize() for
        // details.

        // The KruxStdLib will also handle parsing of app-specific command-line
        // options if you create
        // and pass an OptionParser to it *before* calling its initialize()
        // method. They will
        // be added to the reserved options list (and even show up in the
        // helpful --help output)

        // handle a couple custom cli-params
        OptionParser parser = new OptionParser();
        OptionSpec<String> optionalOption = parser.accepts( "optional-option", "An optional custom option" )
                .withOptionalArg().ofType( String.class ).defaultsTo( "value1" );

        OptionSpec<String> requiredOption = parser.accepts( "required-option", "A required custom option" )
                .withRequiredArg().ofType( String.class ).defaultsTo( "value2" );

        // give parser to KruxStdLib so it can add our params to the reserved
        // list
        KruxStdLib.setOptionParser( parser );

        // Initializing the KruxStdLib will do a few things:
        // 1. The command line args passed to initialize() are parsed and
        // validated. Values for cl options
        // are available in the returned OptionSet object. Unrecognized options
        // will cause the
        // process to terminate with an error sent to stderr.
        // 2. An asynchronous console appender that wraps a
        // is programatically created and added to the rootlogger. See LoggerConfigurator for details.
        // 3. A static statsd client library is instantiated and made available
        // as a static member of KruxStdLib
        // 4. An HTTP server is started that will respond to "/__status"
        // requests with a 200 OK.

        // At any time, an app may register a shutdown hook to be run on nominal
        // process exit.
        // All registered hooks will be run in a single thread (for now) in the
        // order they were
        // added to the list.
        // add another shutdown hook
        KruxStdLib.registerShutdownHook( new ShutdownTask( 50 ) { //shutdown tasks are executed in order of priority
            @Override
            public void run() {
                System.out.print( "This shutdown hook was registered BEFORE initializing KruxStdLib with priority 50" );
            }
        } );

        // add web handlers
        KruxStdLib.registerHttpHandler( "/myHandler", new ExampleServerHandler() );
        KruxStdLib.registerHttpHandler( "/exception", new ExceptionServerHandler() );

        // initialize the lib, watch the magic unfold
        OptionSet options = KruxStdLib.initialize( args );

        // add another shutdown hook
        KruxStdLib.registerShutdownHook( new ShutdownTask( 10 ) {
            @Override
            public void run() {
                System.out.println( "This shutdown hook was registered AFTER initializing KruxStdLib with priority 10" );
            }
        } );

        // get value of an option
        String s = options.valueOf( optionalOption );

        logger.info( "{}: {}", optionalOption.toString(), options.valueOf( optionalOption ) );
        logger.info( "{}: {}", requiredOption.toString(), options.valueOf( requiredOption ) );
        logger.debug( "This was logged using the stdlib logging configuration" );
        logger.warn( "This was logged using the stdlib logging configuration" );
        logger.error( "This was logged using the stdlib logging configuration" );

        int webServerPort = (Integer) options.valueOf( "http-port" );
        logger.info( "Started web server" );

        try {
            throw new Exception( "An exception was thrown" );
        } catch ( Exception e ) {
            logger.error( "Uh oh.", e );
        }
        System.out.print( "This is printed to standard out" );
    }

}
