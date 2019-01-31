package com.krux.stdlib.utils;

import com.krux.stdlib.KruxStdLib;

/**
 * A client for determining if an application is running within the SLA
 *
 * @author David Richards
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

    /**
     * Check a timestamp against the SLA of the application.
     *
     * @param timestamp timestamp of queued message in long format
     */
    public static void checkTs(long timestamp) {
        checkSla(timestamp);
    }

    /**
     * Check a timestamp against the SLA of the application. Used just like checkTs(), but includes a timer stat
     * for message delay segmented by datacenter.
     *
     * @param timestamp timestamp of queued message in long format
     * @param datacenter datacenter of beacon log origin
     */
    public static void checkTs(long timestamp, String datacenter) {
        // Check log TS against current TS
        long logDelay = checkSla(timestamp);

        // Always send timer stat so we can see kafka performance
        KruxStdLib.STATSD.time("message.delay." + datacenter, logDelay);
    }

    /**
     * Returns the current status of the SLA.
     *
     * @return Boolean status of application SLA
     */
    public static Boolean isSlaMet() {

        if ( !_isSlaMet ) {
            Boolean status = _isSlaMet;
            // flip boolean back to true after seen by monitoring
            _isSlaMet = true;
            return status;
        }
        return _isSlaMet;
    }

    /**
     * Check beacon log timestamp against current timestamp to determine if the log is outside of SLA.
     * @param timestamp
     * @return
     */
    private static long checkSla(long timestamp) {
        // get current time and subtract the sla in milliseconds
        long slaMinTimestamp = System.currentTimeMillis() - _slaInMillis;
        // if the message received time is outside the sla threshold
        if ( timestamp < slaMinTimestamp ) {
            // update the value of _isSlaMet. we only set false, because we
            // want to make sure that the failure is seen by monitoring at
            // least once. At that time we flip it back to true.
            _isSlaMet = false;

            // send a failure metric
            KruxStdLib.STATSD.count("sla.failure");
        }
        return timestamp - slaMinTimestamp;
    }

}

