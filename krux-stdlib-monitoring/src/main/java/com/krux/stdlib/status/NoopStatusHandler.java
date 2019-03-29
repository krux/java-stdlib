package com.krux.stdlib.status;

/**
 * Implementation of {@link StatusHandler} which always returns valid stats
 * @author bcottam
 *
 */
public class NoopStatusHandler implements StatusHandler {

    @Override
    public StatusBean getStatus() {
        return new StatusBean(AppState.OK, "all is well");
    }

    @Override
    public String encode(StatusBean status) {
        return "{status: OK}";
    }

}
