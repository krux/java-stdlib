package com.krux.stdlib.statsd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.TimerTask;

import com.krux.stdlib.KruxStdLib;
import com.sun.management.UnixOperatingSystemMXBean;

public class JDKAndSystemStatsdReporter extends TimerTask {

    public JDKAndSystemStatsdReporter() {

    }

    @Override
    public void run() {
        // Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        KruxStdLib.STATSD.gauge( "heap_used", usedMemory );

        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        KruxStdLib.STATSD.gauge( "threads_live", bean.getThreadCount() );

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if ( os instanceof UnixOperatingSystemMXBean ) {
            KruxStdLib.STATSD.gauge( "open_fd", ( (UnixOperatingSystemMXBean) os ).getOpenFileDescriptorCount() );
        }
        
        //cpu util stats
        try {
            String line;
            Process p = Runtime.getRuntime().exec( "mpstat 1 1" );
            BufferedReader bri = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
            BufferedReader bre = new BufferedReader( new InputStreamReader( p.getErrorStream() ) );
            String[] outputs;
            Float usercpu, iowaitcpu;
            int i = 0;
            while ( ( line = bri.readLine() ) != null ) {
                i++;
                if ( i == 5 ) {
                    outputs = line.split( "\\s+" );
                    usercpu = Float.parseFloat( outputs[2] );
                    iowaitcpu = Float.parseFloat( outputs[5] );
                    KruxStdLib.STATSD.gauge( "usr_cpu", usercpu.longValue() );
                    KruxStdLib.STATSD.gauge( "iowait_cpu", iowaitcpu.longValue() );
                }
            }
            bri.close();
            bre.close();
            p.waitFor();
        } catch ( Exception err ) {
            KruxStdLib.STATSD.time( "cpu_collection_error", 1 );
        }

    }

}
