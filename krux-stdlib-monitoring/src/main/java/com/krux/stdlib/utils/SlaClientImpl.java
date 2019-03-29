package com.krux.stdlib.utils;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicLong;

import com.krux.stdlib.statsd.StatsdClient;

/**
 * Default implementation of the {@link SlaClient} API
 * @author bcottam
 *
 */
public class SlaClientImpl implements SlaClient {

    private StatsdClient statsd;
    private Clock clock;
    private long slaMillis;
    private AtomicLong slaFailures = new AtomicLong();

    public SlaClientImpl(StatsdClient statsd, long slaMillis) {
        this(statsd, slaMillis, Clock.systemUTC());
    }

    public SlaClientImpl(StatsdClient statsd, long slaMillis, Clock clock) {
        this.statsd = statsd;
        this.clock = clock;
        this.slaMillis = slaMillis;
    }

    public long getSlaFailureCount() {
        // we get an set so that we start fresh for the next time the status is queried:
        return slaFailures.getAndSet(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkTs(long timestamp) {
        checkSla(timestamp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkTs(long timestamp, String datacenter) {
        // Check log TS against current TS
        long logDelay = checkSla(timestamp);

        // Always send timer stat so we can see kafka performance
        statsd.time("message.delay." + datacenter, logDelay);
    }

    /**
     * Check beacon log timestamp against current timestamp to determine if the log is outside of SLA.
     * @param timestamp
     * @return
     */
    protected long checkSla(long timestamp) {
        // get current time and subtract against beacon timestamp to get delay
        long delay = clock.millis() - timestamp;
        // if the message received time is outside the sla threshold
        if (delay >  slaMillis) {
            // we increment the number of failures we've seen,
            // this is only reset when the monitoring API queries the failure count
            slaFailures.incrementAndGet();

            // send a failure metric
            statsd.count("sla.failure");
        }

        return delay;
    }

}
