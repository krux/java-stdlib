package com.krux.stdlib.status;

/**
 * POJO based API for getting the status of a service.
 * @author bcottam
 *
 */
public interface StatusHandler {

    /**
     * Performs any internal metrics to determine the current state of the service, embedding the
     * results in the returned {@link StatusBean}
     * @return
     */
    StatusBean getStatus();

    /**
     * Produces a {@link String} suitable for the body of an {@code HTTP} response, usually JSON formatted.
     * The encoded status is generally the JSON form of the value returned by {@link #getStatus()}
     * @param status - the status to encode
     * @return
     */
    String encode(StatusBean status);

    /**
     * Returns the result of {@code encode(getStatus())} 
     * @return
     */
    default String getEncodedStatus() {
        return encode(getStatus());
    }

}
