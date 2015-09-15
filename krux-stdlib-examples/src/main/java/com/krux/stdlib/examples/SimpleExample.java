/**
 * 
 */
package com.krux.stdlib.examples;

import com.krux.stdlib.KruxStdLib;

/**
 * @author casspc
 *
 */
public class SimpleExample {

    /**
     * @param args
     */
    public static void main(String[] args) {
        KruxStdLib.STATSD.count("hey");
    }

}
