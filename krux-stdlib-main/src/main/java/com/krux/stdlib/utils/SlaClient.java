package com.krux.stdlib.utils;

/**
 * API wrapper for the statically defined {@link com.krux.stdlib.utils.SlaClient} API.
 * This allows dependency injection, mocking, general goodness.
 * @author bcottam
 *
 */
public interface SlaClient {

    /**
     * Checks the timestamp against the configured SLA
     * @param timestamp
     */
    void checkTs(long timestamp);

    /**
     * Checks the supplied timestamp against the configured SLA and records a statsd message if delay is not within
     * the configured limit.
     * @param timestamp
     * @param datacenter
     */
    void checkTs(long timestamp, String datacenter);

    /**
     * Returns the number of times the SLA has been missed since the last time this method was called.
     * Calling this method resets the failure count to zero.
     * @return
     */
    long getSlaFailureCount();

}
