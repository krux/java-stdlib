/**
 * 
 */
package com.krux.stdlib.statsd;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.graphite.Graphite;
import com.krux.stdlib.KruxStdLib;

/**
 * @author cass
 * 
 */
public class KruxStatsdGraphiteClient implements KruxStatsSender {

    final static Logger log = (Logger) LoggerFactory.getLogger( KruxStatsdGraphiteClient.class );

    final static String keyNamespace;
    static String statsdSuffix;
    static final MetricRegistry metrics = new MetricRegistry();
    static final Map<String,Timer> timers = new HashMap<>();
    static final Map<String,Meter> counters = new HashMap<>();
    static final Map<String,AtomicLong> gaugeValues = new HashMap<>();

    static {
        keyNamespace = KruxStdLib.STASD_ENV.toLowerCase() + "." + KruxStdLib.APP_NAME.toLowerCase() + ".";
        String graphiteHost = "";
        try {
            String hostName = InetAddress.getLocalHost().getHostName().toLowerCase();
            if ( hostName.contains( "pdx" ) ) {
                graphiteHost = "graphite-collector-pdx.krxd.net";
            } else if ( hostName.contains( "dub" ) ) {
                graphiteHost = "graphite-collector-dub.krxd.net";
            } else {
                graphiteHost = "graphite-collector-ash.krxd.net";
            }
            if ( hostName.contains( "." ) ) {
                String[] parts = hostName.split( "\\." );
                hostName = parts[0];
            }
            statsdSuffix = "." + hostName;
        } catch ( Exception e ) {
            log.warn( "Cannot get a real hostname, defaulting to something stupid" );
            statsdSuffix = "." + "unknown";
        }
        
        final Graphite graphite = new Graphite(new InetSocketAddress(graphiteHost, 2029));
        final KruxGraphiteReporter graphiteReporter = KruxGraphiteReporter.forRegistry(metrics)
                                                          .prefixedWith( "stats" )
                                                          .convertRatesTo(TimeUnit.SECONDS)
                                                          .convertDurationsTo(TimeUnit.MILLISECONDS)
                                                          .filter(MetricFilter.ALL)
                                                          .build(graphite);
        graphiteReporter.start(10, TimeUnit.SECONDS);
    }

    public KruxStatsdGraphiteClient() {
    }

    @Override
    public String toString() {
        return KruxStatsdGraphiteClient.class.getName();
    }
    
//    public void shutdown() {
//        super.shutdown();
//    }

    /* (non-Javadoc)
     * @see com.krux.stdlib.statsd.KruxStatsSender#count(java.lang.String)
     */
    @Override
    public void count( String key ) {
        getCounter( key ).mark();
    }
    
    /* (non-Javadoc)
     * @see com.krux.stdlib.statsd.KruxStatsSender#count(java.lang.String, int)
     */
    @Override
    public void count( String key, int count ) {
        getCounter( key ).mark( count );
    }
    
    /* (non-Javadoc)
     * @see com.krux.stdlib.statsd.KruxStatsSender#time(java.lang.String, long)
     */
    @Override
    public void time( String key, long millis ) {
        getTimer( key ).update(millis, TimeUnit.MILLISECONDS);
    }
    
    /* (non-Javadoc)
     * @see com.krux.stdlib.statsd.KruxStatsSender#time(java.lang.String, long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void time( String key, long time, TimeUnit timeunit ) {
        getTimer( key ).update(time, timeunit);
    }
    
    /* (non-Javadoc)
     * @see com.krux.stdlib.statsd.KruxStatsSender#gauge(java.lang.String, long)
     */
    @Override
    public void gauge( String key, long value ) {
        registerGauge( key );
        gaugeValues.get( fullKey( key ) ).set( value );
    }
    
    private void registerGauge(String key) {
        AtomicLong gauge = gaugeValues.get( fullKey( key ) );
        if ( gauge == null ) {
            gaugeValues.put( fullKey( key ), new AtomicLong() );
            metrics.register( fullKey( key ), 
                    new Gauge<AtomicLong>() {
                        
                        @Override
                        public AtomicLong getValue() {
                            return gaugeValues.get( fullKey( key ) );
                        }
                    });
        }
    }

    private Meter getCounter(String key) {
        Meter counter = counters.get( fullKey( key ) );
        if ( counter == null ) {
            counter = metrics.meter( fullKey( key ) );
            counters.put( fullKey( key ), counter );
        }
        return counter;
    }
    
    private Timer getTimer(String key) {
        Timer timer = timers.get( fullKey( key ) );
        if ( timer == null ) {
            timer = metrics.timer( fullKey( key ) );
            timers.put( fullKey( key ), timer );
        }
        return timer;
    }

    private String fullKey( String appKey ) {
        return keyNamespace + appKey.toLowerCase() + statsdSuffix;
    }

}
