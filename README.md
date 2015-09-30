Krux Standard Java Library
==========================

> *Note* - Version 3.0.0 will be released soon!  For a preview of what's changing, see the 
> [3.0 README](https://github.com/krux/java-stdlib/tree/v3)

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
--log-level                                  Default log4j log level (default: DEBUG)                               
--stats                                      Enable/disable statsd broadcast                          
--stats-environment                          Stats environment (dictates statsd prefix) (default: dev)
--stats-host                                 Listening statsd host (default: localhost)               
--stats-port [Integer]                       Listening statsd port (default: 8125) 
```

In more advanced scenarios, you can specifiy custom command line options, set up shutdown hooks, tap into a standard HTTP listener and do other groovy things. See [a detailed example](https://github.com/krux/java-krux-stdlib/blob/master/src/main/java/com/krux/stdlib/sample/ExampleMain.java) for more complex uses.

