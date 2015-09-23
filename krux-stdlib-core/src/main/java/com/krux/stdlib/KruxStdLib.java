/**
 * 
 */
package com.krux.stdlib;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.http.server.HttpServiceManager;
import com.krux.stdlib.logging.LoggingSetupManager;
import com.krux.stdlib.shutdown.ShutdownTask;
import com.krux.stdlib.stats.KruxStatsSender;
import com.krux.stdlib.stats.StatsService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * @author casspc
 * 
 */
public class KruxStdLib {

    static Logger LOGGER = null;

    /**
     * See
     */
    public static KruxStatsSender STATSD = null;// new NoopStatsdClient();
    public static String ENV;
    public static String APP_NAME;
    public static String APP_VERSION;
    public static String BASE_APP_DIR;
    public static String STASD_ENV;

    private static OptionParser _parser = null;
    private static OptionSet _options = null;
    private static boolean _initialized = false;

    public static boolean httpListenerRunning = false;

    public static int HTTP_PORT = 0;

    private static boolean INTIALIZED = false;

    // holds all registered Runnable shutdown hooks (which are executed
    // synchronously in the
    // order added to this list.
    private static Queue<ShutdownTask> shutdownHooks = new PriorityQueue<ShutdownTask>();

    private static String _appDescription = null;

    /**
     * If you'd like to utilize the std lib parser for your app-specific cli
     * argument parsing needs, feel free to pass a configured OptionParser to
     * this class before initializing it. (see this class' source for examples
     * and http://pholser.github.io/jopt-simple/examples.html for details on how
     * to create and configure the OptionParser)
     * 
     * @param parser
     *            a Configured OptionParser
     */
    public static void setOptionParser(OptionParser parser) {
        _parser = parser;
    }

    public static OptionParser getOptionParser() {
        if (_parser == null) {
            _parser = new OptionParser();
        }
        return _parser;
    }

    /**
     * Initializes the std libary static functions, including logging, statsd
     * client and a "status" http listener
     * 
     * @param args
     *            The command line args that were passed to your main( String[]
     *            args )
     * @return the OptionSet of parsed command line arguments
     */
    public static OptionSet initialize(String appDescription, String[] args) {
        _appDescription = appDescription;
        return initialize(args);
    }
    
    public static void initialize() {
        initialize(null);
    }

    /**
     * Initializes the std libary static functions, including logging, statsd
     * client and a "status" http listener
     * 
     * @param args
     *            The command line args that were passed to your main( String[]
     *            args )
     * @return the OptionSet of parsed command line arguments
     */
    public static OptionSet initialize(String[] args) {
        
        LoggingSetupManager.getInstance();
        LOGGER = LoggerFactory.getLogger(KruxStdLib.class.getName());

        if (args == null) {
            args = new String[0];
        }
        
        if (!_initialized) {

            OptionParser parser;
            if (_parser == null) {
                parser = new OptionParser();
            } else {
                parser = _parser;
            }

            List<String> synonyms = asList("help", "h");
            parser.acceptsAll(synonyms, "Prints this helpful message");
            
            OptionSpec enableStatsd = parser.accepts("stats", "Enable/disable statsd broadcast");
            OptionSpec<String> statsdHost = parser.accepts("stats-host", "Listening statsd host").withOptionalArg()
                    .ofType(String.class).defaultsTo("localhost");
            OptionSpec<Integer> statsdPort = parser.accepts("stats-port", "Listening statsd port").withOptionalArg()
                    .ofType(Integer.class).defaultsTo(8125);
            
            OptionSpec<String> environment = parser.accepts("env", "Operating environment").withOptionalArg()
                    .ofType(String.class);
            
            OptionSpec<String> logLevel = parser
                    .accepts("log-level", "Default log4j log level. Valid values: DEBUG, INFO, WARN, " + "ERROR, FATAL")
                    .withOptionalArg().ofType(String.class);
            
            OptionSpec<String> appNameOption = parser
                    .accepts("app-name",
                            "Application identifier, used for statsd namespaces, log file names, etc. "
                                    + "If not supplied, will use this app's entry point classname.")
                    .withOptionalArg().ofType(String.class);
            
            OptionSpec<Integer> httpListenPort = parser
                    .accepts("http-port", "Accept http connections on this port (0 = web server will not start)")
                    .withOptionalArg().ofType(Integer.class);
            OptionSpec<String> baseAppDirectory = parser.accepts("base-dir", "Base directory for app needs.")
                    .withOptionalArg().ofType(String.class);
            OptionSpec<String> statsEnvironment = parser
                    .accepts("stats-environment", "Stats environment (dictates statsd prefix)").withOptionalArg()
                    .ofType(String.class);
            OptionSpec<Integer> heapReporterIntervalMs = parser
                    .accepts("heap-stats-interval-ms", "Interval (ms) for used heap statsd gauge").withOptionalArg()
                    .ofType(Integer.class);
            OptionSpec<Boolean> handleLogRotation = parser
                    .accepts("rotate-logs",
                            "If true, log to a rolling file appender that will keep a maximum of 10 log files, 10MB each")
                    .withOptionalArg().ofType(Boolean.class).defaultsTo(false);

            _options = parser.parse(args);
            
            //print help and bail if requested
            // if "--help" was passed in, show some helpful guidelines and exit
            if (_options.has("help")) {
                try {
                    if (_appDescription != null)
                        System.out.println(_appDescription);
                    parser.formatHelpWith(new KruxHelpFormatter());
                    parser.printHelpOn(System.out);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    System.exit(0);
                }
            }
            
            //we take all the command line values and set them as system properties
            Map<String,Object> commandLineOverrides = new HashMap<>();
            final String kruxStdLibConfigPrefix = "krux.stdlib";
            final String nettyServerConfigPrefix = "netty.web.server";
            final String statsConfigPrefix = "stats";
            
            //legacy-handling command line options overrides
            //web server override
            if (_options.has(httpListenPort)) {
                commandLineOverrides.put(kruxStdLibConfigPrefix+"."+nettyServerConfigPrefix+".http-port", _options.valueOf(httpListenPort));
            }
            
            //app-name override
            if (_options.has(appNameOption)) {
                commandLineOverrides.put(kruxStdLibConfigPrefix+".app-name", _options.valueOf(appNameOption));
            }

            ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults();
            renderOptions.setJson(true);
            Config c = ConfigFactory.load();
            LOGGER.debug( "Default config: {}", c.root().render(renderOptions));
            commandLineOverrides.put("app-name", APP_NAME);
            Config overridesConfig = ConfigFactory.parseMap(commandLineOverrides);
            c = overridesConfig.withFallback(c);
            LOGGER.debug( "New config with CL overrides: {}", c.root().render(renderOptions));
            
            // set base app dir
            BASE_APP_DIR = c.getString("base-dir");
            STASD_ENV = c.getString("env");
            if (c.hasPath("http-port"))
                HTTP_PORT = c.getInt("http-port");

            // set environment
            ENV = c.getString("env");

            // set global app name
            APP_NAME = getMainClassName(c.getString(kruxStdLibConfigPrefix+".app-name"));

            
            if ( INTIALIZED == false ) {
                // setup statsd
                try {
                    STATSD = StatsService.getInstance(c);
                } catch (Exception e) {
                    LOGGER.warn("Cannot establish a statsd connection", e);
                }

                // setup web server
                HttpServiceManager manager = HttpServiceManager.getInstance(c);
                manager.start();
                INTIALIZED = true;
            }

            STATSD.count("process_start");

            // finally, setup a shutdown thread to run all registered
            // application hooks
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    ShutdownTask t;
                    while ((t = shutdownHooks.poll()) != null) {
                        t.run();
                    }
                }
            });

            _initialized = true;
            LOGGER.info("** Started " + APP_NAME + " **");
            for (OptionSpec<?> spec : _options.specs()) {
                LOGGER.info(spec.toString() + " : " + _options.valuesOf(spec));
            }

            // finally, add a shutdown hook that sleeps for 2 sec to allow last
            // log messages to
            // be written

            // make sure we cancel that timer, jic
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2 * 1000);
                    } catch (Exception e) {
                        LOGGER.warn("Error during last shutdown sleep", e);
                    }
                }
            });

        }
        return _options;
    }

    private static String getMainClassName(String name) {

        if (!name.equals("unspecified")) 
            return name;
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement main = stack[stack.length - 1];
        String mainClass = main.getClassName();
        if (mainClass.contains(".")) {
            String[] parts = mainClass.split("\\.");
            mainClass = parts[parts.length - 1];
        }
        String fixedClassName = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(mainClass), '_');
        return fixedClassName.toLowerCase();
    }

    public static void registerShutdownHook(ShutdownTask r) {
        shutdownHooks.add(r);
    }

}
