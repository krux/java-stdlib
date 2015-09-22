/**
 * 
 */
package com.krux.stdlib.stats.statsd;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.stats.KruxStatsSender;
import com.typesafe.config.Config;

/**
 * @author cass
 * 
 */
public class KruxStatsdClient extends StatsdClient implements KruxStatsSender {

    final static Logger LOGGER = (Logger) LoggerFactory.getLogger(KruxStatsdClient.class);

    String _keyNamespace;
    static String _statsdSuffix;

    public KruxStatsdClient() {}

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

    @Override
    public void count(String key) {
        super.count(key);
    }

    @Override
    public void count(String key, int count) {
        super.count(key, count);
    }

    @Override
    public void time(String key, long millis) {
        super.time(key, millis);
    }

    @Override
    public void gauge(String key, long value) {
        stat(StatsdStatType.GAUGE, key, value, 1.0);
    }

    private String fullKey(String appKey) {
        return _keyNamespace + appKey.toLowerCase() + _statsdSuffix;
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize(Config config) {
        LOGGER.info("Initializing {}", this.getClass().getCanonicalName());
        _keyNamespace = config.getString("env").toLowerCase() + "." +
                        config.getString("app-name").toLowerCase() + ".";
        try {
            String hostName = InetAddress.getLocalHost().getHostName().toLowerCase();
            if (hostName.contains(".")) {
                String[] parts = hostName.split("\\.");
                hostName = parts[0];
            }
            _statsdSuffix = "." + hostName;
        } catch (Exception e) {
            LOGGER.warn("Cannot get a real hostname, defaulting to something stupid");
            _statsdSuffix = "." + "unknown";
        }
        
        LOGGER.info("_keyNamespace: {}, _statsdSuffix: {}", _keyNamespace, _statsdSuffix);
    }

    @Override
    public void time(String key, long time, TimeUnit timeunit) {

    }

}