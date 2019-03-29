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
     * Returns true if the <b>last message</b> met the configured sla, false otherwise.
     * After a new message is received, this value may change (i.e.: it is computed upon each message arrival)
     * @return
     */
    boolean isSlaMet();
}
