/**
 * 
 */
package com.krux.stdlib.http.server;

import com.krux.stdlib.AppState;
import com.typesafe.config.Config;

/**
 * @author casspc
 *
 */
public class NoopHttpService implements HttpService {

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void setStatusCodeAndMessage(AppState state, String message) {}

    @Override
    public void resetStatusCodeAndMessageOK() {}

    @Override
    public void addAdditionalStatus(String key, Object value) {}

    @Override
    public void initialize(Config config) {}

}
