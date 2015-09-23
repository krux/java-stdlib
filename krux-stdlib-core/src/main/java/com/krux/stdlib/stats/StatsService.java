/**
 * 
 */
package com.krux.stdlib.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * @author casspc
 *
 */
public class StatsService implements KruxStatsSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsService.class.getName());

    private static StatsService _service;
    private ServiceLoader<KruxStatsSender> _loader;
    private List<KruxStatsSender> _senders = new ArrayList<>();;

    private StatsService(Config config) {
        if (config.hasPath("krux.stdlib.stats")) {
            boolean runStats = config.getBoolean("krux.stdlib.stats.enabled");
            if (runStats) {
                _loader = ServiceLoader.load(KruxStatsSender.class);
                try {
                    Iterator<KruxStatsSender> statsSenders = _loader.iterator();
                    while (statsSenders.hasNext()) {
                        KruxStatsSender sndr = statsSenders.next();
                        sndr.initialize(config);
                        _senders.add(sndr);
                        LOGGER.debug("KruxStatsSender providers loaded: {}", sndr.getClass().getCanonicalName());
                    }
                    if (_senders.size() > 0) {
                        LOGGER.debug("{} KruxStatsSender providers loaded", _senders.size());
                    } else {
                        LOGGER.info("No KruxStatsSender providers found! Using NoopStatsdClient");
                        _senders.add(new NoopStatsdClient());
                    }
                } catch (ServiceConfigurationError serviceError) {
                    LOGGER.error("Cannot instantiate KruxStatsSender", serviceError);
                }
            }
        } else {
            LOGGER.info("Stats not enabled");
        }
    }

    public static synchronized StatsService getInstance(Config config) {
        if (_service == null) {
            _service = new StatsService(config);
        }
        return _service;
    }

    @Override
    public void count(String key) {
        for (KruxStatsSender sender : _senders) {
            sender.count(key);
        }
    }

    @Override
    public void count(String key, int count) {
        for (KruxStatsSender sender : _senders) {
            sender.count(key, count);
        }
    }

    @Override
    public void time(String key, long millis) {
        for (KruxStatsSender sender : _senders) {
            sender.time(key, millis);
        }
    }

    @Override
    public void time(String key, long time, TimeUnit timeunit) {
        for (KruxStatsSender sender : _senders) {
            sender.time(key, time, timeunit);
        }
    }

    @Override
    public void gauge(String key, long value) {
        for (KruxStatsSender sender : _senders) {
            sender.gauge(key, value);
        }
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
        // TODO Auto-generated method stub

    }

}
