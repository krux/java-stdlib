/**
 * 
 */
package com.krux.stdlib.http.server;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.AppState;
import com.krux.stdlib.stats.KruxStatsSender;
import com.krux.stdlib.stats.NoopStatsdClient;
import com.krux.stdlib.stats.StatsService;

import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author casspc
 *
 */
public class HttpServiceManager implements HttpService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServiceManager.class.getName());
    private static HttpServiceManager _manager;
    private static HttpService _service;
    private ServiceLoader<HttpService> _loader;
    
    private HttpServiceManager() {
        _loader = ServiceLoader.load(HttpService.class);
        
        try {
            Iterator<HttpService> statsSenders = _loader.iterator();
            while (statsSenders.hasNext()) {
                _service = statsSenders.next();
            }
            
            if (_service == null) {
                LOGGER.warn("Cannot find an HTTP service provider");
                _service = new NoopHttpService();
            }

        } catch (ServiceConfigurationError serviceError) {
            LOGGER.error("Cannot instantiate KruxStatsSender", serviceError);
        }
    }
    
    public static synchronized HttpServiceManager getInstance() {
        if (_manager == null) {
            _manager = new HttpServiceManager();
        }
        return _manager;
    }

    @Override
    public void start() {
        _service.start();
    }

    @Override
    public void stop() {
        _service.stop();
    }

    @Override
    public void setStatusCodeAndMessage(AppState state, String message) {
        _service.setStatusCodeAndMessage(state, message);
    }

    @Override
    public void resetStatusCodeAndMessageOK() {
        _service.resetStatusCodeAndMessageOK();
    }

    @Override
    public void addAdditionalStatus(String key, Object value) {
        _service.addAdditionalStatus(key, value);
    }

    @Override
    public void registerHttpHandler(String url, ChannelInboundHandlerAdapter handler) {
        _service.registerHttpHandler(url, handler);
    }

}
