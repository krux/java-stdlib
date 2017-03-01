package com.krux.stdlib.utils;

import com.krux.stdlib.KruxStdLib;


/**
 * Created by d.richards on 2/28/17.
 */
public class SlaClient {

    // holds the SLA status value
    private static Boolean _isSlaMet = true;
    // holds the SLA value in millis
    private static long _slaInMillis = KruxStdLib.SLA_IN_SECONDS * 1000;

    // thread safe singleton class
    private static class Loader {
        static SlaClient INSTANCE = new SlaClient();
    }
    private SlaClient () {}
    public static SlaClient getInstance() {
        return Loader.INSTANCE;
    }


    static void checkTs(long timestamp) {

        // get current time and subtract the sla in milliseconds
        long slaMinTimestamp = System.currentTimeMillis() - _slaInMillis;

        // if the message received time is outside the sla threshold
        if ( timestamp < slaMinTimestamp ) {

            // update the value of isSlaMet
            _isSlaMet = false;
        } else {
            _isSlaMet = true;
        }
    }

    public static Boolean isSlaMet() {
        return _isSlaMet;
    }
}

