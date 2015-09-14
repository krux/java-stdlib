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

/**
 * @author casspc
 *
 */
public class StatsService implements KruxStatsSender {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StatsService.class.getName());
    
    private static StatsService _service;
    private ServiceLoader<KruxStatsSender> _loader;
    private List<KruxStatsSender> _senders;
    
    private StatsService() {
        _loader = ServiceLoader.load(KruxStatsSender.class);
        _senders = new ArrayList<>();
        try {
            Iterator<KruxStatsSender> statsSenders = _loader.iterator();
            while (statsSenders.hasNext()) {
                _senders.add(statsSenders.next());
            }
            if (_senders.size() > 0) {
                LOGGER.info("{} KruxStatsSender providers loaded", _senders.size());
            } else {
                LOGGER.warn("No KruxStatsSender providers found! Using NoopStatsdClient");
                _senders.add(new NoopStatsdClient());
            }
        } catch (ServiceConfigurationError serviceError) {
            LOGGER.error("Cannot instantiate KruxStatsSender", serviceError);;
        } 
    }
    
    public static synchronized StatsService getInstance() {
        if (_service == null) {
            _service = new StatsService();
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

}
