package com.krux.stdlib.statsd;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.TimerTask;

import com.krux.stdlib.KruxStdLib;

public class HeapStatsdReporter extends TimerTask {

    public HeapStatsdReporter() {
        
    }

    @Override
    public void run() {
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        KruxStdLib.statsd.gauge( "heap_used", usedMemory );
        
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        KruxStdLib.statsd.gauge( "threads_live", bean.getThreadCount() );
    }

}
