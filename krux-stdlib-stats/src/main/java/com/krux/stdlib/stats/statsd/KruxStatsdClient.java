/**
 * 
 */
package com.krux.stdlib.stats.statsd;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.DatagramChannel;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;
import com.krux.stdlib.shutdown.ShutdownTask;
import com.krux.stdlib.stats.KruxStatsSender;
import com.krux.stdlib.stats.jvm.JDKAndSystemStatsdReporter;
import com.krux.stdlib.stats.statsd.etsy.StatsdClient;
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

    @Override
    public String toString() {
        return KruxStatsdClient.class.getName();
    }

    @Override
    public void count(String key) {
        super.increment(fullKey(key));
    }

    @Override
    public void count(String key, int count) {
        super.increment(fullKey(key), count);
    }

    @Override
    public void time(String key, long millis) {
        super.timing(fullKey(key), millis);
    }

    @Override
    public void gauge(String key, long value) {
        super.gauge(fullKey(key), value);
    }

    private String fullKey(String appKey) {
        return _keyNamespace + appKey.toLowerCase() + _statsdSuffix;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void initialize(Config config) {
        LOGGER.debug("Initializing {}", this.getClass().getCanonicalName());
        _keyNamespace = config.getString("krux.stdlib.env").toLowerCase() + "." +
                        config.getString("krux.stdlib.app-name").toLowerCase() + ".";
        
        int port = config.getInt("krux.stdlib.stats.port");
        String statsDServer = config.getString("krux.stdlib.stats.host");
        
        String hostName = "unspecified-host";
        try {
            hostName = InetAddress.getLocalHost().getHostName().toLowerCase();
            if (hostName.contains(".")) {
                String[] parts = hostName.split("\\.");
                hostName = parts[0];
            }
            _statsdSuffix = "." + hostName;
        } catch (Exception e) {
            LOGGER.warn("Cannot get a real hostname, defaulting to something stupid");
            _statsdSuffix = "." + "unknown";
        }
        
        LOGGER.debug("_keyNamespace: {}, _statsdSuffix: {}", _keyNamespace, _statsdSuffix);
        LOGGER.debug("statsDServer: {}, port: {}", statsDServer, port);
        
        _address = new InetSocketAddress(statsDServer, port);
        
        try {
            _channel = DatagramChannel.open();
            /* Put this in non-blocking mode so send does not block forever. */
            _channel.configureBlocking(false);
            /*
             * Increase the size of the output buffer so that the size is larger
             * than our buffer size.
             */
            _channel.setOption(StandardSocketOptions.SO_SNDBUF, 4096);
            setBufferSize((short) 1500);
        } catch (Exception e) {
            LOGGER.error("Cannot setup statsd client", e);
        }
        
        //start jvm stats reporting
        JDKAndSystemStatsdReporter jvmReporter = new JDKAndSystemStatsdReporter(this);
        final Timer t = new Timer();
        t.schedule(jvmReporter, 5000, config.getLong("krux.stdlib.stats.jvm-stats-interval-ms"));
        
        KruxStdLib.registerShutdownHook( new ShutdownTask(100) {
            @Override
            public void run() {
                t.cancel();
            }
        });
        
    }

    @Override
    public void time(String key, long time, TimeUnit timeunit) {

    }

}