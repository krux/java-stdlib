package com.krux.stdlib.utils;

import com.krux.stdlib.KruxStdLib;
import com.krux.stdlib.status.StatusHandler;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Created by d.richards on 2/28/17.
 */
public class SlaClientTest {

    private long failureTS;
    private long successTS;
    private KruxStdLib stdLib;
    private StatusHandler handler = new DummyStatusHandler();


    @Before
    public void setUp() throws Exception {

        URL testPropertyFileURL = Thread.currentThread().getContextClassLoader().getResource("test_properties.properties");

        // initialize
        stdLib = new KruxStdLib();
        KruxStdLib.ArgBuilder builder = new KruxStdLib.ArgBuilder().withPropertyFileName(testPropertyFileURL.getPath());
        stdLib.initialize(stdLib.parse(builder), handler);

        // ensure the timestamp will fail by subtracting 1 milli from the oldest possible
        // successful timestamp
        failureTS = System.currentTimeMillis() - (stdLib.getSlaInSeconds() * 1000) - 1;

        // save a timestamp that should return a successful SLA status
        successTS = System.currentTimeMillis();
    }

    @Test
    public void verifySingletonStatusTest() {

        // get the singleton class
        SlaClient slaClient = stdLib.getSlaClient();

        // add the failing timestamp
        slaClient.checkTs(failureTS);

        // ensure the sla status has false
        Boolean status = slaClient.isSlaMet();
        assertEquals(false, status);

        // get the singleton class again
        SlaClient slaClient2 = stdLib.getSlaClient();

        // ensure the sla status is still failing
        boolean status2 = slaClient2.isSlaMet();
        assertEquals(true, status2);
    }

    @Test
    public void ensureNormalOperationTest() {

        // get the singleton class
        SlaClient slaClient = stdLib.getSlaClient();

        // send a timestamp that should return successful
        slaClient.checkTs(successTS);

        // check sla status
        boolean status = slaClient.isSlaMet();

        // verify the success
        assertEquals(true, status);
    }

    @Test
    public void ensureThreadSafeTest() throws InterruptedException {

        // setup testing class
        class SingletonTestRunnable implements Runnable {

            private volatile long timestamp;
            private volatile boolean expectedValue;
            private volatile boolean actual;

            public SingletonTestRunnable(long timestamp, boolean expectedValue) {
                this.timestamp = timestamp;
                this.expectedValue = expectedValue;
            }


            public void run() {
                // Get a reference to the singleton.
                SlaClient slaClient = stdLib.getSlaClient();

                // ensure the value is correct
                actual = slaClient.isSlaMet();

                // send one timestamp
                slaClient.checkTs(this.timestamp, "ash");
            }
        }

        SingletonTestRunnable one = new SingletonTestRunnable(failureTS, true);
        SingletonTestRunnable two = new SingletonTestRunnable(successTS, false);
        SingletonTestRunnable three = new SingletonTestRunnable(failureTS, true);
        // setup the threads
        Thread threadOne = new Thread(one),
                threadTwo = new Thread(two),
                threadThree = new Thread(three);

        // start and run the threads
        threadOne.start();
        threadTwo.start();
        threadThree.start();

        // join the threads
        threadOne.join();
        threadTwo.join();
        threadThree.join();

        assertEquals(one.expectedValue, one.actual);
        assertEquals(two.expectedValue, two.actual);
        assertEquals(three.expectedValue, three.actual);
    }
}
