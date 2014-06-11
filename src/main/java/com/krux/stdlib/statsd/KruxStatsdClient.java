/**
 * 
 */
package com.krux.stdlib.statsd;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ubercraft.statsd.StatsdClient;
import org.ubercraft.statsd.StatsdStatType;

import com.krux.stdlib.KruxStdLib;

/**
 * @author cass
 * 
 */
public class KruxStatsdClient extends StatsdClient {
    
    final static Logger log = (Logger) LoggerFactory.getLogger(KruxStatsdClient.class);
    
    final static String keyNamespace;
    static String statsdSuffix;
    
    static {
        keyNamespace = KruxStdLib.statsdEnv.toLowerCase() + "." + KruxStdLib.appName.toLowerCase() + ".";
        try {
            statsdSuffix = "." + InetAddress.getLocalHost().getHostName().toLowerCase();
        } catch ( Exception e ) {
            log.warn( "Cannot get a real hostname, defaulting to something stupid" );
            statsdSuffix = "." + "unknown";
        }
    }

    public KruxStatsdClient(String host, int port, Logger logger) throws Exception {
        super(host, port, logger);
    }

    @Override
    public String toString() {
        return KruxStatsdClient.class.getName();
    }

    public long getQueueOfferTimeout() {
        return super.getQueueOfferTimeout();
    }

    public void setQueueOfferTimeout(long queueOfferTimeout) {
        super.setQueueOfferTimeout(queueOfferTimeout);
    }

    public void shutdown() {
        super.shutdown();
    }

    public boolean count(String key) {
       return super.count( key );
    }

    public boolean count(String key, int count) {
        return super.count( key, count );
    }

    public boolean count(String key, double sampleRate) {
        return super.count( key, sampleRate );
    }

    public boolean count(String key, int count, double sampleRate) {
        return super.count( key, count, sampleRate );
    }

    public boolean time(String key, long millis) {
        return super.time( key, millis );
    }

    public boolean time(String key, long millis, double sampleRate) {
        return super.time( key, millis, sampleRate );
    }

    public boolean gauge(String key, int value) {
        return super.gauge( key, value );
    }

    public boolean stat(StatsdStatType type, String key, long value, double sampleRate) {
        return super.stat( type, fullKey(key), value, sampleRate );
    }
    
    private String fullKey( String appKey ) {
        return keyNamespace + appKey.toLowerCase() + statsdSuffix;
    }

}
