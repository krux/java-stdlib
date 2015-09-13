package com.krux.stdlib;

import java.util.concurrent.TimeUnit;

public interface KruxStatsSender {

    public abstract void count(String key);

    public abstract void count(String key, int count);

    public abstract void time(String key, long millis);

    public abstract void time(String key, long time, TimeUnit timeunit);

    public abstract void gauge(String key, long value);

}