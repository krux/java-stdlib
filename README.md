java-krux-stdlib
================

The Krux standard java library handles logging, command-line parsing, statsd reporting and other common functions for Krux applications.

# Using KruxStdLib

For the simplest use case, simply call `KruxStdLIb.initialize(args)` before running your app's main logic...

```java
public class ExampleMain {
	KruxStdLib.initialize(args);

	//...do your stuff here...
}
```

This will setup sl4j (via log4j) bindings for stdout and stderr, establish a statsd client for use throughout your app, and parse a standard set of command line options that all Krux apps should support. To see a list of the standard command line options, build an app that uses the stdlib as above, then pass '-h' or '--help' at the command line.  You will see output like...

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

In more advanced scenarios, you can specifiy custom command line options, set up shutdown hooks, tap into a standard HTTP listener and do other groocy things. See https://github.com/krux/java-krux-stdlib/blob/master/src/main/java/com/krux/stdlib/sample/ExampleMain.java for more complex examples.

