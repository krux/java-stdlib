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
public class NewSchoolAPIWithCustomWebHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NewSchoolAPIWithCustomWebHandler.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        // new-school API.  No KruxStdLib.initialize() necessary
        KruxStats.count("starting_custom_web_demo");
        
        

        
        
    }

}
