Krux Standard Java Library
==========================

<!-- markdown-toc start - Don't edit this section. Run M-x markdown-toc-refresh-toc -->
**Table of Contents**

- [Krux Standard Java Library](#krux-standard-java-library)
- [Using](#using)
- [Basic Usage](#basic-usage)
- [Setting up the SLA handler](#setting-up-the-sla-handler)
- [Logging Configuration](#logging-configuration)

<!-- markdown-toc end -->

The Krux Sandard Library is an easily-configurable library that provides a handful of capabilities common across all of Krux' JVM-based applications, including:

* A [Netty](http://netty.io)-based web server for handling HTTP-based status checks and custom URL handlers for app-specific needs
* A standard logging configuration, based on [SLF4J](http://www.slf4j.org), that ensures all logged events with severity >= WARN are writting to STDERR, and those with a severity <= INFO are written to STDOUT, which can be very useful for environments where individual application lifecycles are managed by external tooling that has strong opinions about log handling (such as [supervisord](http://supervisord.org))
* A [Statsd](https://github.com/etsy/statsd) client that automatically collects and submits common JVM statistics to a Statsd server, and a API for collecting and submitting custom application metrics
* A handful of useful tools for command-line parsing, managing a priority-based queue of application shutdown hooks, and other helpful utilities.

(If you're looking for our Python Standard Library, see [here](https://github.com/krux/python-krux-stdlib)).

# Using

To use, add the following dependency to your pom.xml:

```xml
<dependency>
  <groupId>com.krux</groupId>
  <artifactId>krux-stdlib</artifactId>
  <version>1.7.2</version>
</dependency>
```

# Basic Usage

For the simplest use case, simply call `KruxStdLib.initialize(args)` before running your app's main logic...

```java
public class ExampleMain {

		public static void main(String[] args) {
			KruxStdLib.initialize(args);

            //send a simple stat
            KruxStdLib.STATSD.count("my-app-started");
            
			//...do your stuff...
			
			//by default, will be written to STDOUT
			LOGGER.info("I've really done something.");
			
			//by default, will be written to STERR
            LOGGER.warn("I MUST DO MORE!");
	}
}
```

# Setting up the SLA handler

Example from a consumer service sending message timestamps to the `SlaClient` for verification.

```java
import com.krux.stdlib.utils.SlaClient;

public class messageProcessor { 
    
    private static SlaClient _slaClient = SlaClient.getInstance();
    
    public onMessage(String message) {
       // send the message to the sla client to determine if the sla has been met
       SlaClient.checkTs(GifStreamParserUtil.getTSInMillis(recordParts));
    }
}
```

Testing the SLA endpoint

```bash
curl localhost:8080/__sla
```


This will setup slf4j (via log4j) bindings for stdout and stderr, establish a statsd client for use throughout your app via `KruxStdLib.STATSD`, and parse a standard set of command line options that all Krux apps should support. To see a list of the standard command line options, build an app that uses the stdlib as above, then pass '-h' or '--help' at the command line.  You will see output like...

```
Option                                       Description                                              
------                                       -----------                                              
--app-name                                   Application identifier, used for statsd namespaces, log  
                                               file names, etc. If not supplied, will use this app's  
                                               entry point classname. (default:                       
                                               TCPStreamListenerServer)                               
--base-dir                                   Base directory for app needs. (default: /tmp)            
--env                                        Operating environment (default: dev)                     
-h, --help                                   Prints this helpful message                              
--heap-stats-interval-ms [Integer]           Interval (ms) for used heap statsd gauge (default: 1000) 
--http-port [Integer]                        Accept http connections on this port (0 = web server     
                                               will not start) (default: 0)                           
--log-level                                  DEPRECATED - logs warning, has no effect
--stats                                      Enable/disable statsd broadcast                          
--stats-environment                          IGNORED for backward compatibility                       
--stats-host                                 Listening statsd host (default: localhost)               
--stats-port [Integer]                       Listening statsd port (default: 8125)
--property-file [String]                     Path to an external property file, containing names of external resources
                                             such that vary by environment, such as a database server hostname.
--sla                                        SLA in seconds to return on the /__sla endpoint                                          
```

In more advanced scenarios, you can specifiy custom command line options, set up shutdown hooks, tap into a standard HTTP listener and do other groovy things. See [a detailed example](https://github.com/krux/java-krux-stdlib/blob/master/src/main/java/com/krux/stdlib/sample/ExampleMain.java) for more complex uses.

# Logging Configuration

**Important:** The `--log-level` option has been deprecated and will log a warning. <i>It is doubltful that it actually worked, because <b>SLF4J does not have runtime log level configuration.</b></i> How nobody noticed this is a mystery for the ages. Here's a fun read: a [decade old ticket](https://jira.qos.ch/browse/SLF4J-124) to add this capability.

The only supported method for logging configuration is `log4j2.properties` file. See *[Apache Logging Services: Configuration][log4j2-configuration]* and the example in `krux-stdlib-examples/src/main/resources` for more information. The latter also shows how to enable setting log level by setting a `KRUX_LOGGER_LEVEL` environment variable.


[log4j2-configuration]: https://logging.apache.org/log4j/2.x/manual/configuration.html
