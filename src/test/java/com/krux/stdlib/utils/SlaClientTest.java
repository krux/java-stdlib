package com.krux.stdlib.utils;

import com.krux.stdlib.KruxStdLib;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Created by d.richards on 2/28/17.
 */
public class SlaClientTest {

    private static final Logger LOGGER = LoggerFactory.getLogger( ExternalProperties.class.getName() );

    private static long failureTS;
    private static long successTS;
    private static KruxStdLib kruxStdLib;

    @Before
    public void setUp() throws Exception {

        URL testPropertyFileURL = Thread.currentThread().getContextClassLoader().getResource("test_properties.properties");

        // initialize
        kruxStdLib = new KruxStdLib();
        kruxStdLib.initialize(null, false, null, null, null, null, null, null, null, null, testPropertyFileURL.getPath());

        // ensure the timestamp will fail by subtracting 1 milli from the oldest possible
        // successful timestamp
        failureTS = System.currentTimeMillis() - (KruxStdLib.SLA_IN_SECONDS * 1000) - 1;

        // save a timestamp that should return a successful SLA status
        successTS = System.currentTimeMillis();
    }

    @Test
    public void verifySingletonStatusTest() {

        // get the singleton class
        SlaClient slaClient = SlaClient.getInstance();

        // add the failing timestamp
        slaClient.checkTs(failureTS);

        // ensure the sla status has false
        Boolean status = slaClient.isSlaMet();
        assertEquals(false, status);

        // get the singleton class again
        SlaClient slaClient2 = SlaClient.getInstance();

        // ensure the sla status is still failing
        Boolean status2 = slaClient2.isSlaMet();
        assertEquals(true, status2);
    }

    @Test
    public void ensureNormalOperationTest() {

        // get the singleton class
        SlaClient slaClient = SlaClient.getInstance();

        // send a timestamp that should return successful
        slaClient.checkTs(successTS);

        // check sla status
        Boolean status = slaClient.isSlaMet();

        // verify the success
        assertEquals(true, status);
    }

    @Test
    public void ensureThreadSafeTest() throws InterruptedException {

        // setup testing class
        class SingletonTestRunnable implements Runnable {

            private volatile long timestamp;
            private volatile Boolean expectedValue;

            public SingletonTestRunnable(long timestamp, Boolean expectedValue) {
                this.timestamp = timestamp;
                this.expectedValue = expectedValue;
            }


            public void run() {
                // Get a reference to the singleton.
                SlaClient slaClient = SlaClient.getInstance();

                // ensure the value is correct
                assertEquals(this.expectedValue, slaClient.isSlaMet());

                // send one timestamp
                slaClient.checkTs(this.timestamp, 'ash');

            }
        }

        // setup the threads
        Thread threadOne = new Thread(new SingletonTestRunnable(failureTS, true)),
                threadTwo = new Thread(new SingletonTestRunnable(successTS, false)),
                threadThree = new Thread(new SingletonTestRunnable(failureTS, true));

        // start and run the threads
        threadOne.start();
        threadTwo.start();
        threadThree.start();

        // join the threads
        threadOne.join();
        threadTwo.join();
        threadThree.join();
    }
}
