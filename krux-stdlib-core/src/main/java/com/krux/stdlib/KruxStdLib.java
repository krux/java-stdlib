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
    public static String STATS_ENV;

    private static OptionParser _parser = null;
    private static OptionSet _options = null;
    private static boolean _initialized = false;

    public static boolean INTIALIZED = false;

    // holds all registered Runnable shutdown hooks (which are executed
    // synchronously in the
    // order added to this list.
    private static Queue<ShutdownTask> shutdownHooks = new PriorityQueue<ShutdownTask>();

    private static String _appDescription = null;

    private static final String kruxStdLibConfigPrefix = "krux.stdlib";
    private static final String nettyServerConfigPrefix = "netty.web.server";
    private static final String loggingServerConfigPrefix = "logging";
    private static final String statsConfigPrefix = "stats";
    private static Config _runningConfig = null;
    
    private static final String STDLIB_WEB_PROPERTIES_PREFIX = kruxStdLibConfigPrefix + "." + nettyServerConfigPrefix;
    private static final String STDLIB_LOGGING_PROPERTIES_PREFIX = kruxStdLibConfigPrefix + "." + loggingServerConfigPrefix;
    private static final String STDLIB_STATS_PROPERTIES_PREFIX = kruxStdLibConfigPrefix + "." + statsConfigPrefix;
    private static final String STDLIB_BASEDIR_PROPERTY = kruxStdLibConfigPrefix + ".base-dir";
    private static final String STDLIB_APPNAME_PROPERTY = kruxStdLibConfigPrefix + ".app-name";

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
    public static OptionSet initialize(String appDescription, String[] args, Config c) {
        _appDescription = appDescription;
        return initialize(args, c);
    }
    
    public static OptionSet initialize(String appDescription, String[] args) {
        return initialize(appDescription, args, (Config)null);
    }

    public static void initialize(Config c) {
        initialize(null, c);
    }
    
    public static void initialize() {
        initialize(null, (Config)null);
    }
    
    public static OptionSet initialize(String[] args) {
        return initialize(args, (Config)null);
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
    public static OptionSet initialize(String[] args, Config config) {

        try {
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
                OptionSpec<Boolean> handleLogRotation = parser
                        .accepts("rotate-logs",
                                "If true, log to a rolling file appender that will keep a maximum of 10 log files, 10MB each")
                        .withOptionalArg().ofType(Boolean.class).defaultsTo(false);

                _options = parser.parse(args);

                // print help and bail if requested
                // if "--help" was passed in, show some helpful guidelines and
                // exit
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

                // legacy-handling command line options overrides
                Map<String, Object> commandLineOverrides = new HashMap<>();
                
                // web server override
                if (_options.has(httpListenPort)) {
                    commandLineOverrides.put(STDLIB_WEB_PROPERTIES_PREFIX + ".http-port",
                            _options.valueOf(httpListenPort));
                }

                // app-name override
                if (_options.has(appNameOption)) {
                    commandLineOverrides.put(STDLIB_APPNAME_PROPERTY, StringUtils
                            .join(StringUtils.splitByCharacterTypeCamelCase(_options.valueOf(appNameOption)), '_'));
                }

                // env override
                if (_options.has(environment)) {
                    commandLineOverrides.put(kruxStdLibConfigPrefix + ".env", _options.valueOf(environment));
                }

                // base-app override
                if (_options.has(baseAppDirectory)) {
                    commandLineOverrides.put(STDLIB_BASEDIR_PROPERTY, _options.valueOf(baseAppDirectory));
                }

                // log-level override
                if (_options.has(logLevel)) {
                    commandLineOverrides.put(STDLIB_LOGGING_PROPERTIES_PREFIX + ".log-level",
                            _options.valueOf(logLevel));
                }
                
                // rotate logs
                if (_options.has(handleLogRotation)) {
                    commandLineOverrides.put(STDLIB_LOGGING_PROPERTIES_PREFIX + ".rotate-logs", _options.valueOf(handleLogRotation));
                }
                
                // statsd host
                if (_options.has(statsdHost)) {
                    commandLineOverrides.put(STDLIB_STATS_PROPERTIES_PREFIX + ".host", _options.valueOf(statsdHost));
                }
                
                // statsd port
                if (_options.has(statsdPort)) {
                    commandLineOverrides.put(STDLIB_STATS_PROPERTIES_PREFIX + ".port", _options.valueOf(statsdPort));
                }
                
                // statsd env
                if (_options.has(statsEnvironment)) {
                    commandLineOverrides.put(STDLIB_STATS_PROPERTIES_PREFIX + ".env", _options.valueOf(statsEnvironment));
                }
                
                ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults();
                renderOptions.setJson(true);
                if ( config == null ) {
                    config = ConfigFactory.load();
                }
                if (!config.hasPath(STDLIB_APPNAME_PROPERTY)
                        && !commandLineOverrides.containsKey(STDLIB_APPNAME_PROPERTY)) {
                    commandLineOverrides.put(STDLIB_APPNAME_PROPERTY, getMainClassName());
                }

                Config overridesConfig = ConfigFactory.parseMap(commandLineOverrides);
                config = overridesConfig.withFallback(config);

                LoggingSetupManager.getInstance(config);
                LOGGER = LoggerFactory.getLogger(KruxStdLib.class.getName());

                // set base app dir
                BASE_APP_DIR = config.getString(STDLIB_BASEDIR_PROPERTY);
                STATS_ENV = config.getString(STDLIB_STATS_PROPERTIES_PREFIX + ".env");

                // set environment
                ENV = config.getString(kruxStdLibConfigPrefix + ".env");
                APP_NAME = config.getString(STDLIB_APPNAME_PROPERTY);

                if (INTIALIZED == false) {
                    // setup statsd
                    try {
                        STATSD = StatsService.getInstance(config);
                    } catch (Exception e) {
                        LOGGER.warn("Cannot establish a statsd connection", e);
                    }

                    // setup web server
                    HttpServiceManager manager = HttpServiceManager.getInstance(config);
                    manager.start();
                    INTIALIZED = true;
                }

                STATSD.count("process_start");
                _runningConfig = config;

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

                LOGGER.warn("** Started " + APP_NAME + " **");
                for (OptionSpec<?> spec : _options.specs()) {
                    LOGGER.info(spec.toString() + " : " + _options.valuesOf(spec));
                }

                // finally, add a shutdown hook that sleeps for 2 sec to allow
                // last
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
        } catch (Exception e) {
            e.printStackTrace(System.err);
            LOGGER.error("Cannot initialize KruxStdLib", e);
        }
        return _options;
    }

    private static String getMainClassName() {

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

    public static Config getConfig() {
        return _runningConfig;
    }

}
