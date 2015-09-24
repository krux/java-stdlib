Krux Standard Java Library
==========================

The Krux Sandard Library is an easily-configurable library that provides a handful of capabilities common across all of Krux' JVM-based applications, including:

* A [Netty](http://netty.io)-based web server for handling HTTP-based status checks and custom URL handlers for app-specific needs
* A standard logging configuration, based on [SLF4J](http://www.slf4j.org), that ensures all logged events with severity >= WARN are writting to STDERR, and those with a severity <= INFO are written to STDOUT, which can be very useful for environments where individual application lifecycles are managed by external tooling that has strong opinions about log handling (such as [supervisord](http://supervisord.org))
* A [Statsd](https://github.com/etsy/statsd) client that automatically collects and submits common JVM statistics to a Statsd server, and a API for collecting and submitting custom application metrics

It also provides a handful of useful tools for command-line parsing, managing a priority-based queue of application shutdown hooks, and other helpful utilities.  

(If you're looking for our Python Standard Library, see [here](https://github.com/krux/python-krux-stdlib)).

# Using

As of version 3 of the Krux java Standard Library, each of the core capabilities it provides is made available via distinct build dependencies. The project contains several sub-modules:

* [krux-stdlib-core](https://github.com/krux/java-stdlib/tree/v3/krux-stdlib-core) Provides common APIs and interfaces. Most of the classes individual applications will use are contained in this project.
* [krux-stdlib-stats](https://github.com/krux/java-stdlib/tree/v3/krux-stdlib-stats) Provides configuration and implementations for metrics gathering and transmission via a StatsD client
* [krux-stdlib-logging](https://github.com/krux/java-stdlib/tree/v3/krux-stdlib-logging) Provides SLF4J implementations of appenders that router logged events to STDOUT or STDERR based on the event's severity.
* [krux-stdlib-netty-web](https://github.com/krux/java-stdlib/tree/v3/krux-stdlib-netty-web) Provides a Netty-based web server that, by default, will respond to requests for "/__status" with application metrics and health information.  Custom HTTP URL handlers can be added to the server for custom application web serving needs.
* [krux-stdlib-examples](https://github.com/krux/java-stdlib/tree/v3/krux-stdlib-examples) A collection of simple demo applications that show off the Std Lib's capabilities.

The Stats, Logging and Netty Web sub-modules all depend on the Core module.  To use one or more of the above, simply include the appropriate dependencies in your build configuration.  For example, to make use of the Stats package but not the Logging or Netty Web modules in your Maven-based project, just include the Stats package in your pom.xml...

```xml
<dependency>
    <groupId>com.krux</groupId>
    <artifactId>krux-stdlib-stats</artifactId>
    <version>3.0.0-alpha</version>
</dependency>
```

If you want to use the Std Lib' Stats, Logging and Web Server capabilities, drop all three in your pom...
```xml
<dependency>
    <groupId>com.krux</groupId>
    <artifactId>krux-stdlib-logging</artifactId>
    <version>3.0.0-alpha</version>
</dependency>
<dependency>
    <groupId>com.krux</groupId>
    <artifactId>krux-stdlib-stats</artifactId>
    <version>3.0.0-alpha</version>
</dependency>
<dependency>
    <groupId>com.krux</groupId>
    <artifactId>krux-stdlib-netty-web</artifactId>
    <version>3.0.0-alpha</version>
</dependency>
```

# Basic Usage

For the simplest use cases, you can just start using the Std Lib APIs and your SLF4J Loggers as you always have. (For those upgrading from versions older than 3, note the absence of any call to `KruxStdLib.initialize()`). 

```java
public class ExampleMain {

		public static void main(String[] args) {
			
            //send a simple stat
            KruxStats.count("my-app-started");
            
			//...do your stuff...
			
			//by default, will be written to STDOUT
			LOGGER.info("I've really done something.");
			
			//by default, will be written to STERR
            LOGGER.warn("I MUST DO MORE!");
	}
}
```

This will setup SLF4J bindings for stdout and stderr, establish a Statsd client for use throughout your app via `KruxStats`, and if you've included the Netty-Web module, start an embedded web server running on port 8080 (the default, easily configurable).

# Configuration

## Basics

All Std Lib configuration is based on [Typesafe's config library](https://github.com/typesafehub/config).  Each Std Lib sub-module contains sensible default configurations compiled into thier `reference.conf` files. For example, here's the Stats' package's `reference.conf`:

```json
krux.stdlib : {

    ## enables the sending of stats to the specified host/port   
    stats : {
        enabled : true,
        env : dev,
        host : "localhost",
        port : 8125,
        jvm-stats-interval-ms : 5000
    }
}
```
...and here's the Netty-web's [`reference.conf`](https://github.com/krux/java-stdlib/blob/v3/krux-stdlib-netty-web/src/main/resources/reference.conf):

```json
krux.stdlib : {

    ## enables a simple, netty-based HTTP server
    netty.web.server : {
        enabled : true,
        http-port : 8080
    }
}
```

At runtime, all `reference.conf` files found on the classpath are merged into a single global configuration with all of the Std Lib's configuration elements under the `krux.stdlib` namespace.

### Overriding Module Default Configurations

Consuming applications can override module defaults by including an `application.conf` file on their classpath. To force the embedded Netty web server to use port 9999 instead of the default 8080 and send StatsD stats to a remote machine, an app could include the following in its `application.conf` file:

```json
krux.stdlib : {

    ## override the defualt port for HTTP server
    netty.web.server : {
        http-port : 9999
    }
       
    ## send statsd udp packets elsewhere
    stats : {
        host : "a.different.machine.org",
    }
}
```


Design
------
todo


Command Line Parsing
--------------------

To see a list of the standard command line options, build an app that uses the stdlib as above, then pass '-h' or '--help' at the command line.  You will see output like...

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
