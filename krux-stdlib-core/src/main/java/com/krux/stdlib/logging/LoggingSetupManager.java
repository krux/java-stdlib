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
import com.typesafe.config.ConfigFactory;

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
                LOGGER.info("Found {} logging services", count);
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
    
    public synchronized static LoggingSetupManager getInstance() {
        return getInstance(ConfigFactory.load());
    }
    
    /* (non-Javadoc)
     * @see com.krux.stdlib.KruxStdLibService#start()
     */
    @Override
    public void start() {

    }

    /* (non-Javadoc)
     * @see com.krux.stdlib.KruxStdLibService#stop()
     */
    @Override
    public void stop() {

    }

    /* (non-Javadoc)
     * @see com.krux.stdlib.KruxStdLibService#initialize(com.typesafe.config.Config)
     */
    @Override
    public void initialize(Config config) {
    }

}
