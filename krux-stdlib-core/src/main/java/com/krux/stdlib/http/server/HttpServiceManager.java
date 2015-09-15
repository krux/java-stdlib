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
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

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

    private HttpServiceManager(Config config) {
        boolean runHttpServer = config.getBoolean("krux.stdlib.netty.web.server.enabled");
        if (runHttpServer) {
            _loader = ServiceLoader.load(HttpService.class);

            try {
                Iterator<HttpService> statsSenders = _loader.iterator();
                while (statsSenders.hasNext()) {
                    _service = statsSenders.next();
                    _service.initialize(config);
                }

                if (_service == null) {
                    LOGGER.warn("Cannot find an HTTP service provider");
                    _service = new NoopHttpService();
                }

            } catch (ServiceConfigurationError serviceError) {
                LOGGER.error("Cannot instantiate KruxStatsSender", serviceError);
            }
        } else {
            LOGGER.info("Netty web server not enabled");
        }
    }

    public static synchronized HttpServiceManager getInstance(Config config) {
        if (_manager == null) {
            _manager = new HttpServiceManager(config);
        }
        return _manager;
    }

    public static synchronized HttpServiceManager getInstance() {
        return getInstance(ConfigFactory.load());
    }

    @Override
    public void start() {
        if (_service != null)
            _service.start();
    }

    @Override
    public void stop() {
        if (_service != null)
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

    @Override
    public void initialize(Config config) {}

}
