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

        for (int i = 0; i < 10000; i++) {
            KruxStdLib.STATSD.time("hello.time", i);
        }
    }

}
