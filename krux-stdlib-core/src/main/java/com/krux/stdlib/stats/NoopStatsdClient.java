/**
 * 
 */
package com.krux.stdlib.stats;

import java.util.concurrent.TimeUnit;

import com.typesafe.config.Config;

/**
 * @author cass
 * 
 */
public class NoopStatsdClient implements KruxStatsSender {

    public NoopStatsdClient() {}

    @Override
    public void count(String key) {
        // TODO Auto-generated method stub

    }

    @Override
    public void count(String key, int count) {
        // TODO Auto-generated method stub

    }

    @Override
    public void time(String key, long millis) {
        // TODO Auto-generated method stub

    }

    @Override
    public void time(String key, long time, TimeUnit timeunit) {
        // TODO Auto-generated method stub

    }

    @Override
    public void gauge(String key, long value) {
        // TODO Auto-generated method stub

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
