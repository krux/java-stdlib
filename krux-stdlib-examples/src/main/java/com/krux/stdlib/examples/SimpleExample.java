/**
 * 
 */
package com.krux.stdlib.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;
import com.krux.stdlib.stats.KruxStats;

/**
 * @author casspc
 *
 */
public class SimpleExample {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleExample.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        // old-school - will throw a null pointer because KruxStdLib.initialize() was never called
        try {
            KruxStdLib.STATSD.count("hey");
        } catch (Exception e) {
            LOGGER.info("We expected that: {}", e.getLocalizedMessage());
        }
        
        // new-school API.  No KruxStdLib.initialize() necessary
        KruxStats.count("there");

        //now this will work, too, because KruxStats calls KruxStdLib.initialize() under the covers;
        for (int i = 0; i < 10000; i++) {
            KruxStdLib.STATSD.time("hello.time", i);
        }
        
        KruxStats.gauge("my_height", 72);
    }

}
