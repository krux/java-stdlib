package com.krux.stdlib;

import com.krux.server.http.StdHttpServer;
import com.krux.server.http.StdHttpServerHandler;
import com.krux.stdlib.shutdown.ShutdownTask;
import com.krux.stdlib.statsd.JDKAndSystemStatsdReporter;
import com.krux.stdlib.statsd.KruxStatsdClient;
import com.krux.stdlib.statsd.NoopStatsdClient;
import com.krux.stdlib.statsd.StatsdClient;
import com.krux.stdlib.status.NoopStatusHandler;
import com.krux.stdlib.status.StatusHandler;
import com.krux.stdlib.status.StatusHandlerWrapper;
import com.krux.stdlib.utils.NoopSlaClient;
import com.krux.stdlib.utils.SlaClient;
import com.krux.stdlib.utils.SlaClientImpl;
import io.netty.channel.ChannelInboundHandlerAdapter;
import joptsimple.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import static java.util.Arrays.asList;

/**
 * @author casspc
 *
 */
public class KruxStdLib {

  private static final Logger LOGGER = LoggerFactory.getLogger(KruxStdLib.class);

  private static final String KRUX_APP_NAME_PROPERTY = "krux.app.name";
  private static final String KRUX_APP_DIR_PROPERTY = "krux.app.dir";
  private static final String KRUX_ENVIRONMENT_PROPERTY = "krux.environment";
  private static final String KRUX_LOGGER_LEVEL_PROPERTY = "krux.logger.level";
  private static final String KRUX_STATS_ENVIRONMENT_PROPERTY = "krux.stats.environment";
  private static final String KRUX_STATS_HOST_PROPERTY = "krux.stats.host";
  private static final String KRUX_STATS_PORT_PROPERTY = "krux.stats.port";
  private static final String KRUX_STATS_ENABLED_PROPERTY = "krux.stats.enabled";

  private static volatile KruxStdLib instance;

  /**
   * Returns the statically tracked instance of {@link KruxStdLib}.
   * This is generally set during the {@link #initialize(OptionSet, StatusHandler)} method
   * (which should only really happen once per JVM).
   */
  public static KruxStdLib get() {
    if (instance == null) {
      instance = new KruxStdLib();
      // empty instance config, to avoid null pointers for time when
      // people don't initialize correctly
      instance.parseAndInitialize(new String[] {});
    }
    return instance;
  }

  /**
   * Utility setter that allows anyone to replace the instance of {@link KruxStdLib} if desired
   * (generally only appropriate for tests)
   * @param instance
   */
  public static void set(KruxStdLib instance) {
    KruxStdLib.instance = instance;
  }

  public static class ArgBuilder {

    private boolean enableStatsd = true;
    private String statsdHost;
    private Integer statsdPort;
    private String statsdEnvironment;
    private String environment;
    private Level loggingLevel;
    private String applicationName;
    private Integer httpListenPort;
    private String baseAppDirectory;
    private String propertyFileName;

    public ArgBuilder withEnableStatsd(boolean enableStatsd) {
      this.enableStatsd = enableStatsd;
      return this;
    }

    public ArgBuilder withStatsdHost(String statsdHost) {
      this.statsdHost = statsdHost;
      return this;
    }

    public ArgBuilder withStatsdPort(Integer statsdPort) {
      this.statsdPort = statsdPort;
      return this;
    }

    public ArgBuilder withStatsdEnvironment(String statsdEnvironment) {
      this.statsdEnvironment = statsdEnvironment;
      return this;
    }

    public ArgBuilder withEnvironment(String environment) {
      this.environment = environment;
      return this;
    }

    public ArgBuilder withLoggingLevel(Level loggingLevel) {
      this.loggingLevel = loggingLevel;
      return this;
    }

    public ArgBuilder withApplicationName(String applicationName) {
      this.applicationName = applicationName;
      return this;
    }

    public ArgBuilder withHttpListenPort(Integer httpListenPort) {
      this.httpListenPort = httpListenPort;
      return this;
    }

    public ArgBuilder withBaseAppDirectory(String baseAppDirectory) {
      this.baseAppDirectory = baseAppDirectory;
      return this;
    }

    public ArgBuilder withPropertyFileName(String propertyFileName) {
      this.propertyFileName = propertyFileName;
      return this;
    }

    public String[] toArgs() {
      List<String> params = new ArrayList<String>();
      if ( enableStatsd ) {
        params.add( "--stats" );

        if ( statsdHost != null ) {
          set(statsdHost, "--stats-host", params);
          set(statsdPort, "--stats-port", params);
        }

        set(statsdEnvironment, "--stats-environment", params);
      }

      set(environment, "--env", params);
      set(loggingLevel, "--log-level", params);
      set(applicationName, "--app-name", params);
      set(httpListenPort, "--http-port", params);
      set(baseAppDirectory, "--base-dir", params);
      set(propertyFileName, "--property-file", params);

      return params.toArray(new String[params.size()]);
    }

    protected void set(Object value, String cliFlag, List<String> params) {
      if (value != null) {
        params.add(cliFlag);
        params.add(String.valueOf(value));
      }
    }
  }

  private OptionParser parser = null;
  private OptionSet options = null;
  private boolean initialized = false;

  private boolean httpListenerRunning = false;

  // holds all registered Runnable shutdown hooks (which are executed
  // synchronously in the
  // order added to this list.
  private Queue<ShutdownTask> shutdownHooks = new PriorityQueue<ShutdownTask>();

  // holds list of registered ChannelInboundHandlerAdapters for serving http
  // responses
  private Map<String, ChannelInboundHandlerAdapter> httpHandlers = new HashMap<String, ChannelInboundHandlerAdapter>();

  private String appDescription = null;

  private int httpPort = 0;
  private String env;
  private String appName;
  private String appVersion;
  private String baseAppDir;
  private String logLevel = "WARN";
  private Integer heapReporterIntervalMs;
  private boolean handleLogRotation = false;
  private String configFileLocation;

  private boolean enableStatsd;
  private String statsdEnv;
  private String statsdHost;
  private Integer statsdPort;

  private String propertyFile;
  private Integer slaInSeconds;

  private StatsdClient statsdClient = new NoopStatsdClient();
  private SlaClient slaClient = new NoopSlaClient();
  private StatusHandler statusHandler = null;

  public OptionSet getOptions() {
    return options;
  }

  public String getEnv() {
    return env;
  }

  public String getAppName() {
    return appName;
  }

  public String getAppVersion() {
    return appVersion;
  }

  public String getAppDescription() {
    return appDescription;
  }

  public String getBaseAppDir() {
    return baseAppDir;
  }

  public String getStatsdEnv() {
    return statsdEnv;
  }

  public String getPropertyFile() {
    return propertyFile;
  }

  public Integer getSlaInSeconds() {
    return slaInSeconds;
  }

  public boolean isHttpListenerRunning() {
    return httpListenerRunning;
  }

  public Map<String, ChannelInboundHandlerAdapter> getHttpHandlers() {
    return httpHandlers;
  }

  public int getHttpPort() {
    return httpPort;
  }

  public String getLogLevel() {
    return logLevel;
  }

  public Integer getHeapReporterIntervalMs() {
    return heapReporterIntervalMs;
  }

  public boolean isHandleLogRotation() {
    return handleLogRotation;
  }

  public String getConfigFileLocation() {
    return configFileLocation;
  }

  public boolean isEnableStatsd() {
    return enableStatsd;
  }

  public String getStatsdHost() {
    return statsdHost;
  }

  public Integer getStatsdPort() {
    return statsdPort;
  }

  public StatsdClient getStatsdClient() {
    return statsdClient;
  }

  public SlaClient getSlaClient() {
    return slaClient;
  }

  public StatusHandler getStatusHandler() {
    return statusHandler;
  }

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
  public void setOptionParser( OptionParser parser ) {
    this.parser = parser;
  }

  public OptionParser getOptionParser() {
    if ( this.parser == null ) {
      this.setOptionParser(new OptionParser());
    }
    return this.parser;
  }

  public OptionSet parseAndInitialize(String appDescription, String[] args) {
    OptionSet optionSet = parse(appDescription, args);
    initialize(optionSet, new NoopStatusHandler());
    return optionSet;
  }

  public OptionSet parseAndInitialize(ArgBuilder builder) {
    return parseAndInitialize(builder.applicationName, builder.toArgs());
  }

  public OptionSet parseAndInitialize(String[] args) {
    OptionSet optionSet = this.parse(args);
    initialize(optionSet, new NoopStatusHandler());
    return optionSet;
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
  public OptionSet parse(String appDescription, String[] args) {
    this.appDescription = appDescription;
    return parse( args );
  }

  /** Overload for when you don't already have a string[] from the cl **/
  public OptionSet parse(ArgBuilder builder) {
    return parse(appDescription, builder.toArgs());
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
  public OptionSet parse(String[] args) {

    OptionSet optionSet = options;
    if ( !initialized ) {
      // parse command line, handle common needs

      // some defaults
      final String defaultStatsdHost = System.getProperty(KRUX_STATS_HOST_PROPERTY, "localhost");
      final int defaultStatsdPort = Integer.parseInt(System.getProperty(KRUX_STATS_PORT_PROPERTY, "8125"));
      final String defaultEnv = System.getProperty(KRUX_ENVIRONMENT_PROPERTY, "dev");
      final String defaultLogLevel = System.getProperty(KRUX_LOGGER_LEVEL_PROPERTY, "WARN");
      final String defaultAppName = System.getProperty(KRUX_APP_NAME_PROPERTY, getMainClassName());
      final String baseAppDirDefault = System.getProperty(KRUX_APP_DIR_PROPERTY, "/tmp");
      // TODO - remove statsEnvironment and related
      final String statsEnvironmentDefault = System.getProperty(KRUX_STATS_ENVIRONMENT_PROPERTY, "dev");
      final int slaInSecondsDefault = 300;
      final Integer httpListenerPort = 0;
      final int defaultHeapReporterIntervalMs = 1000;

      OptionParser parser = getOptionParser();

      List<String> synonyms = asList( "help", "h" );
      parser.acceptsAll( synonyms, "Prints this helpful message" ).forHelp();
      OptionSpec<Void> enableStatsdOption = parser.accepts( "stats", "Enable/disable statsd broadcast" );
      OptionSpec<String> statsdHostOption = parser.accepts( "stats-host", "Listening statsd host" )
        .withOptionalArg()
        .ofType( String.class )
        .defaultsTo( defaultStatsdHost );
      OptionSpec<Integer> statsdPortOption = parser.accepts( "stats-port", "Listening statsd port" )
        .withOptionalArg()
        .ofType( Integer.class )
        .defaultsTo( defaultStatsdPort );
      OptionSpec<String> environmentOption = parser.accepts( "env", "Operating environment" )
        .withOptionalArg()
        .ofType( String.class )
        .defaultsTo( defaultEnv );
      OptionSpec<String> logLevelOption = parser
        .accepts( "log-level", "Does nothing, retained for backward compat.")
        .withOptionalArg()
        .ofType( String.class )
        .defaultsTo( defaultLogLevel );
      OptionSpec<String> appNameOption = parser
        .accepts(
                 "app-name",
                 "Application identifier, used for statsd namespaces, log file names, etc. If not supplied, will use this app's entry point classname."
                 )
        .withOptionalArg()
        .ofType( String.class )
        .defaultsTo( defaultAppName );
      OptionSpec<Integer> httpListenPortOption = parser.accepts( "http-port", "Accept http connections on this port (0 = web server will not start)" )
        .withOptionalArg()
        .ofType( Integer.class )
        .defaultsTo( httpListenerPort );
      OptionSpec<String> baseAppDirectoryOption = parser.accepts( "base-dir", "Base directory for app needs." )
        .withOptionalArg()
        .ofType( String.class )
        .defaultsTo( baseAppDirDefault );
      // TODO - remove statsEnvironment and related
      OptionSpec<String> statsEnvironmentOption = parser.accepts( "stats-environment", "IGNORED for backwards compatibility." )
        .withOptionalArg()
        .ofType( String.class )
        .defaultsTo( statsEnvironmentDefault );
      OptionSpec<Integer> heapReporterIntervalMsOption = parser.accepts( "heap-stats-interval-ms", "Interval (ms) for used heap statsd gauge" )
        .withOptionalArg()
        .ofType( Integer.class )
        .defaultsTo( defaultHeapReporterIntervalMs );
      OptionSpec<Boolean> handleLogRotationOption = parser
        .accepts(
                 "rotate-logs",
                 "If true, log to a rolling file appender that will keep a maximum of 10 log files, 10MB each"
                  )
        .withOptionalArg()
        .ofType( Boolean.class )
        .defaultsTo( false );

      /* The file passed in via --configuration-file-location is not parsed by KruxStdLib, just made
         available to the calling application for it to parse. */
      OptionSpec<String> configFileLocationOption = parser.accepts("configuration-file-location", "configuration file location for app needs.")
        .withOptionalArg()
        .ofType(String.class);

      /* the --property-file is parsed by Properties and the values are available via
         ExternalProperties.getPropertyValue() */
      OptionSpec<String> propertyFileNameOption = parser.accepts("property-file", "provide environment-specific properties.")
        .withOptionalArg()
        .ofType(String.class);

      /* the --property-file is parsed by Properties and the values are available via
         ExternalProperties.getPropertyValue() */
      OptionSpec<Integer> slaInSecondsOption = parser.accepts("sla", "Sets the SLA in seconds for messages in kafka pipeline.")
        .withOptionalArg()
        .ofType(Integer.class)
        .defaultsTo(slaInSecondsDefault);

      optionSet = parser.parse( args );

      if (optionSet.has(logLevelOption)) {
        LOGGER.warn("--log-level is DEPRECATED and has no effect, see Logging Configuration in the README.md file for more information.");
      }

      // set base app dir
      baseAppDir = optionSet.valueOf( baseAppDirectoryOption );
      httpPort = optionSet.valueOf( httpListenPortOption );
      propertyFile = optionSet.valueOf( propertyFileNameOption );
      slaInSeconds = optionSet.valueOf( slaInSecondsOption );
      env = optionSet.valueOf( environmentOption );
      appName = optionSet.valueOf( appNameOption );

      heapReporterIntervalMs = optionSet.valueOf(heapReporterIntervalMsOption);
      handleLogRotation = optionSet.has(handleLogRotationOption);
      configFileLocation = optionSet.valueOf(configFileLocationOption);

      enableStatsd = optionSet.has(enableStatsdOption);
      // TODO - remove statsEnvironment and related
      statsdEnv = optionSet.valueOf( statsEnvironmentOption );
      statsdHost = optionSet.valueOf(statsdHostOption);
      statsdPort = optionSet.valueOf(statsdPortOption);

      LOGGER.info("Using baseAppDir: {}", baseAppDir);
      LOGGER.info("Using httpPort: {}", httpPort);
      LOGGER.info("Using propertyFile: {}", propertyFile);
      LOGGER.info("Using slaInSeconds: {}", slaInSeconds);
      LOGGER.info("Using env: {}", env);
      LOGGER.info("Using appName: {}", appName);
      LOGGER.info("Using heapReporterIntervalMs: {}", heapReporterIntervalMs);
      LOGGER.info("Using handleLogRotation: {}", handleLogRotation);
      LOGGER.info("Using configFileLocation: {}", configFileLocation);

      LOGGER.info("Using enableStatsd: {}", enableStatsd);
      LOGGER.info("Using statsdEnv: {}", statsdEnv);
      LOGGER.info("Using statsdHost: {}", statsdHost);
      LOGGER.info("Using statsdPort: {}", statsdPort);

      // Set system properties for things that care
      System.setProperty(KRUX_ENVIRONMENT_PROPERTY, env);
      System.setProperty(KRUX_APP_NAME_PROPERTY, appName);
      System.setProperty(KRUX_APP_DIR_PROPERTY, baseAppDir);
      System.setProperty(KRUX_LOGGER_LEVEL_PROPERTY, logLevel);
      System.setProperty(KRUX_STATS_ENABLED_PROPERTY, enableStatsd ? "true" : "false");
      System.setProperty(KRUX_STATS_HOST_PROPERTY, statsdHost);
      System.setProperty(KRUX_STATS_PORT_PROPERTY, statsdPort.toString());
      // TODO - remove statsEnvironment and related
      System.setProperty(KRUX_STATS_ENVIRONMENT_PROPERTY, statsdEnv);
    }

    return optionSet;
  }

  public void initialize(OptionSet options, StatusHandler statusHandler) {

    if (!initialized) {
      OptionParser parser = getOptionParser();
      this.statusHandler = statusHandler;
      this.options = options;

      // if "--help" was passed in, show some helpful guidelines and exit
      if ( options.has( "help" ) ) {
        try {
          if ( this.appDescription != null ) {
            System.out.println( this.appDescription );
          }
          HelpFormatter helpFormatter = new BuiltinHelpFormatter(120, 5);
          parser.formatHelpWith(helpFormatter);
          parser.printHelpOn( System.out );
        } catch ( IOException e ) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } finally {
          System.exit( 0 );
        }
      }

      Properties appProps = new Properties();
      try {
        appProps.load( StdHttpServerHandler.class.getClassLoader().getResourceAsStream( "application.properties" ) );
        appVersion = appProps.getProperty( "app.pom.version", "n/a" );
      } catch ( Exception e ) {
        // Ignore. LOGGER.warn("Cannot load application properties");
      }

      // setup statsd
      try {
        // this one is not like the others. passing "--stats", with or
        // without a value, enables statsd
        if (enableStatsd) {
          LOGGER.info( "statsd metrics enabled" );
          statsdClient = new KruxStatsdClient(statsdHost, statsdPort, LOGGER, this );
        } else {
          statsdClient = new NoopStatsdClient();
        }
      } catch ( Exception e ) {
        LOGGER.warn( "Cannot establish a statsd connection", e );
      }

      statsdClient.count( "process_start" );

      if (slaInSeconds != null) {
        slaClient = new SlaClientImpl(statsdClient, slaInSeconds * 1000L);
      }

      // finally, setup a shutdown thread to run all registered
      // application hooks
      Runtime.getRuntime().addShutdownHook( new Thread() {
          @Override
          public void run() {
            shutdownHooks.forEach((t) -> t.run());
            shutdownHooks.clear();
          }
        } );

      // setup a simple maintenance timer for reporting used heap size
      // and other stuff in the future
      final TimerTask timerTask = new JDKAndSystemStatsdReporter(statsdClient);
      final Timer timer = new Timer( true );
      timer.scheduleAtFixedRate( timerTask, 2 * 1000, heapReporterIntervalMs );

      // make sure we cancel that time, jic
      Runtime.getRuntime().addShutdownHook( new Thread() {
          @Override
          public void run() {
            try {
              timer.cancel();
            } catch ( Exception e ) {
              LOGGER.warn( "Error while attemptin to shut down heap reporter", e );
            }
          }
        } );

      // set up an http listener if the submitted port != 0
      // start http service on a separate thread
      if ( httpPort != 0 ) {
        Thread t = new Thread( new StdHttpServer( httpPort, this ) );
        t.setName( "MainHttpServerThread" );
        t.start();
        httpListenerRunning = true;
      } else {
        // Ignore LOGGER.warn( "Not starting HTTP listener, cli option 'http-port' is not set" );
      }

      initialized = true;
      LOGGER.info( "** Started " + appName + " **" );
      for ( OptionSpec<?> spec : options.specs() ) {
        LOGGER.info( "{} : {}", spec, options.valuesOf( spec ) );
      }

      // finally, add a shutdown hook that sleeps for 2 sec to allow last
      // log messages to
      // be written

      // make sure we cancel that timer, jic
      Runtime.getRuntime().addShutdownHook( new Thread() {
          @Override
          public void run() {
            try {
              Thread.sleep( 2 * 1000 );
            } catch ( Exception e ) {
              LOGGER.warn( "Error during last shutdown sleep", e );
            }
          }
        } );

      // set this instance to be the statically available instance:
      set(this);
    }
  }

  private static String getMainClassName() {

    StackTraceElement[] stack = Thread.currentThread().getStackTrace();
    StackTraceElement main = stack[stack.length - 1];
    String mainClass = main.getClassName();
    if ( mainClass.contains( "." ) ) {
      String[] parts = mainClass.split( "\\." );
      mainClass = parts[parts.length - 1];
    }
    String fixedClassName = StringUtils.join( StringUtils.splitByCharacterTypeCamelCase( mainClass ), '_' );
    return fixedClassName.toLowerCase();
  }

  public void registerShutdownHook( ShutdownTask r ) {
    shutdownHooks.add( r );
  }

  public void registerStatusHandler(String url, StatusHandler handler, StatsdClient statsd) {
    StatusHandlerWrapper wrapper = new StatusHandlerWrapper(handler, statsd);
    registerHttpHandler(url, wrapper);
  }

  public void registerHttpHandler( String url, ChannelInboundHandlerAdapter handler ) {
    if ( !initialized ) {
      if ( !url.contains( "__status") && !url.contains("__sla") ) {
        httpHandlers.put( url, handler );
      }
    }
  }

  public void registerDefaultHttpHandler(ChannelInboundHandlerAdapter handler) {
    if ( !initialized ) {
      httpHandlers.put( "__default", handler );
    }
  }

}
