package com.krux.stdlib.stats;

import java.util.concurrent.TimeUnit;

import com.krux.stdlib.KruxStdLibService;

public interface KruxStatsSender extends KruxStdLibService {

    public abstract void count(String key);

    public abstract void count(String key, int count);

    public abstract void time(String key, long millis);

    public abstract void time(String key, long time, TimeUnit timeunit);

    public abstract void gauge(String key, long value);

}