package com.krux.stdlib.stats;

import java.util.concurrent.TimeUnit;

import com.krux.stdlib.KruxStdLib;

public class KruxStats {
    
    private final static StatsService _statsService;
    
    static {
        KruxStdLib.initialize();
        _statsService = StatsService.getInstance(KruxStdLib.getConfig());
    }

    public static void count(String key) {
        _statsService.count(key);
    }

    public static void count(String key, int count) {
        _statsService.count(key, count);
    }

    public static void time(String key, long millis) {
        _statsService.time(key, millis);
    }

    public static void time(String key, long time, TimeUnit timeunit) {
        _statsService.time(key, time, timeunit);
    }

    public static void gauge(String key, long value) {
        _statsService.gauge(key, value);
    }

}
