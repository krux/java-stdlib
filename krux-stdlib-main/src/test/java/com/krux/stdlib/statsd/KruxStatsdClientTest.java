package com.krux.stdlib.statsd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import joptsimple.OptionSet;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.krux.stdlib.KruxStdLib;
import com.krux.stdlib.status.StatusHandler;
import com.krux.stdlib.statsd.KruxStatsdClient;

import com.krux.stdlib.utils.DummyStatusHandler;

public class KruxStatsdClientTest {
    private KruxStdLib stdlib;
    private StatusHandler handler = new DummyStatusHandler();

    @Before
    public void setUp() throws Exception {
        URL testPropertyFileURL = Thread.currentThread().getContextClassLoader().getResource("test_properties.properties");

        // initialize
        stdlib = new KruxStdLib();
        KruxStdLib.ArgBuilder builder = new KruxStdLib.ArgBuilder().
            withPropertyFileName(testPropertyFileURL.getPath()).
            withEnableStatsd(true);

        stdlib.initialize(stdlib.parse(builder), handler);
    }

    @Test
    public void testInit() throws Exception {
        StatsdClient ksc = stdlib.getStatsdClient();
        assertNotNull(ksc);
    }
}
