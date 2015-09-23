/**
 * 
 */
package com.krux.stdlib.logging;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

/**
 * @author casspc
 *
 */
public class LoggingSetupManager implements LoggingSetupService {

    private static Logger LOGGER = null;
    private ServiceLoader<LoggingSetupService> _loader;
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static LoggingSetupManager _manager;

    private LoggingSetupManager(Config config) {
        initialized.set(true);
        boolean setupLogging = config.hasPath("krux.stdlib.logging");
        if (setupLogging) {
            _loader = ServiceLoader.load(LoggingSetupService.class);
            try {
                Iterator<LoggingSetupService> loggingServices = _loader.iterator();
                int count = 0;
                while (loggingServices.hasNext()) {
                    LoggingSetupService service = loggingServices.next();
                    service.initialize(config);
                    count++;
                }
                LOGGER = LoggerFactory.getLogger(LoggingSetupManager.class.getName());
                LOGGER.debug("Found {} krux stdlib logging service providers on the classpath", count);
            } catch (ServiceConfigurationError serviceError) {
                try {
                    LOGGER.error("Cannot instantiate LoggingSetupManager", serviceError);
                } catch (Exception e) {
                    e.printStackTrace();
                    serviceError.printStackTrace();
                }
            }
        } else {
            LOGGER.info("Netty web server not enabled");
        }
    }

    public synchronized static LoggingSetupManager getInstance(Config config) {
        if (_manager == null) {
            _manager = new LoggingSetupManager(config);
        }
        return _manager;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void initialize(Config config) {}

}
