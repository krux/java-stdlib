package com.krux.stdlib.utils;

import java.time.Clock;
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
    private boolean slaMet = true;

    public SlaClientImpl(StatsdClient statsd, long slaMillis) {
        this(statsd, slaMillis, Clock.systemUTC());
    }

    public SlaClientImpl(StatsdClient statsd, long slaMillis, Clock clock) {
        this.statsd = statsd;
        this.clock = clock;
        this.slaMillis = slaMillis;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSlaMet() {
        // first capture the current status of the sla so it can be returned
        boolean isMet = slaMet;

        // now switch it back to true ('cause we've reported the status to the caller)
        // the reason for this is that checkSla never sets slaMet = true (it only records failures)
        slaMet = true;
        return isMet;
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
            // update the value of _isSlaMet. we only set false, because we
            // want to make sure that the failure is seen by monitoring at
            // least once. At that time we flip it back to true.
            slaMet = false;

            // send a failure metric
            statsd.count("sla.failure");
        }

        return delay;
    }

}
