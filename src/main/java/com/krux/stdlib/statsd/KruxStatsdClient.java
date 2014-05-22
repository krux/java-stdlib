/**
 * 
 */
package com.krux.stdlib.statsd;

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
    
    final static Logger logger = (Logger) LoggerFactory.getLogger(KruxStatsdClient.class);
    
    final static String keyNamespace;
    
    static {
        keyNamespace = KruxStdLib.env + "." + KruxStdLib.appName + ".";
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
       return super.count( keyNamespace + key );
    }

    public boolean count(String key, int count) {
        return super.count( keyNamespace + key, count );
    }

    public boolean count(String key, double sampleRate) {
        return super.count( keyNamespace + key, sampleRate );
    }

    public boolean count(String key, int count, double sampleRate) {
        return super.count( keyNamespace + key, count, sampleRate );
    }

    public boolean time(String key, long millis) {
        return super.time( keyNamespace + key, millis );
    }

    public boolean time(String key, long millis, double sampleRate) {
        return super.time( keyNamespace + key, millis, sampleRate );
    }

    public boolean gauge(String key, int value) {
        return super.gauge( keyNamespace + key, value );
    }

    public boolean stat(StatsdStatType type, String key, long value, double sampleRate) {
        return super.stat( type,  keyNamespace + key, value, sampleRate );
    }

}
