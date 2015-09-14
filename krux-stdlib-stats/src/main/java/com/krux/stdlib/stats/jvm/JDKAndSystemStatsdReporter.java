package com.krux.stdlib.stats.jvm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.TimerTask;

import com.krux.stdlib.stats.KruxStatsSender;
import com.sun.management.UnixOperatingSystemMXBean;

@SuppressWarnings("restriction")
public class JDKAndSystemStatsdReporter extends TimerTask {
    
    private KruxStatsSender _sender;

    public JDKAndSystemStatsdReporter( KruxStatsSender sender ) {
        _sender = sender;
    }

    @Override
    public void run() {
        // Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        _sender.gauge("heap_used", usedMemory);

        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        _sender.gauge("threads_live", bean.getThreadCount());

        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof UnixOperatingSystemMXBean) {
            _sender.gauge("open_fd", ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount());
        }

        // cpu util stats
        try {
            String line;
            Process p = Runtime.getRuntime().exec("mpstat 1 1");
            BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String[] outputs;
            Float usercpu, iowaitcpu;
            int i = 0;
            while ((line = bri.readLine()) != null) {
                i++;
                if (i == 5) {
                    outputs = line.split("\\s+");
                    usercpu = Float.parseFloat(outputs[2]);
                    iowaitcpu = Float.parseFloat(outputs[5]);
                    _sender.gauge("usr_cpu", usercpu.longValue());
                    _sender.gauge("iowait_cpu", iowaitcpu.longValue());
                }
            }
            bri.close();
            bre.close();
            p.waitFor();
        } catch (Exception err) {
            _sender.time("cpu_collection_error", 1);
        }

        // cpu util stats
        try {
            String line;
            Process p = Runtime.getRuntime().exec("cat /proc/loadavg");
            BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String[] outputs;
            Float oneMin, fiveMin, fifteenMin;
            int i = 0;
            while ((line = bri.readLine()) != null) {
                i++;
                if (i == 1) {
                    outputs = line.split("\\s+");
                    oneMin = Float.parseFloat(outputs[0]);
                    fiveMin = Float.parseFloat(outputs[1]);
                    fifteenMin = Float.parseFloat(outputs[2]);
                    _sender.gauge("load_avg.1min", oneMin.longValue());
                    _sender.gauge("load_avg.5min", fiveMin.longValue());
                    _sender.gauge("load_avg.15min", fifteenMin.longValue());
                }
            }
            bri.close();
            bre.close();
            p.waitFor();
        } catch (Exception err) {
            _sender.time("cpu_collection_error", 1);
        }

    }

}
