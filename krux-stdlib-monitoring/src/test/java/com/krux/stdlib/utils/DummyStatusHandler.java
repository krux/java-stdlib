package com.krux.stdlib.utils;

import com.fasterxml.jackson.jr.ob.JSON;
import com.krux.stdlib.status.AppState;
import com.krux.stdlib.status.StatusBean;
import com.krux.stdlib.status.StatusHandler;

public class DummyStatusHandler implements StatusHandler {

    @Override
    public StatusBean getStatus() {
        return new StatusBean(AppState.OK, "all is well");
    }

    @Override
    public String encode(StatusBean status) {
        String encoded = status.getAppStatus().toString();
        try {
            encoded = JSON.std.asString(status);
        } catch (Exception e) {
            // ignore...
        }

        return encoded;
    }

}
