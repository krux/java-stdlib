package com.krux.stdlib.utils;

/**
 * Super-duper simple implementation of the {@link SlaClient} api that does... nothing.
 * 
 * @author bcottam
 *
 */
public class NoopSlaClient implements SlaClient {

    @Override
    public void checkTs(long timestamp) {  }

    @Override
    public void checkTs(long timestamp, String datacenter) {  }

    @Override
    public long getSlaFailureCount() {
        return 0;
    }

}
