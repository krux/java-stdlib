package com.krux.stdlib.utils;

import com.krux.stdlib.KruxStdLib;
import com.krux.stdlib.status.StatusHandler;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.concurrent.CountDownLatch;

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
        long failures = slaClient.getSlaFailureCount();
        assertEquals(1, failures);

        // get the singleton class again
        SlaClient slaClient2 = stdLib.getSlaClient();

        // ensure the sla status is still failing
        long failures2 = slaClient2.getSlaFailureCount();
        assertEquals(0, failures2);
    }

    @Test
    public void ensureNormalOperationTest() {

        // get the singleton class
        SlaClient slaClient = stdLib.getSlaClient();

        // send a timestamp that should return successful
        slaClient.checkTs(successTS);

        // verify the success
        assertEquals(0, slaClient.getSlaFailureCount());
    }

    @Test
    public void ensureThreadSafeTest() throws InterruptedException {

        // setup testing class
        class SingletonTestRunnable implements Runnable {

            private CountDownLatch latch;
            private CountDownLatch counter;
            private long timestamp;
            private long expectedValue;
            private long actual;

            public SingletonTestRunnable(long timestamp, long expectedValue, CountDownLatch latch, CountDownLatch counter) {
                this.latch = latch;
                this.counter = counter;
                this.timestamp = timestamp;
                this.expectedValue = expectedValue;
            }

            private void await() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            public void run() {
                // Get a reference to the singleton.
                SlaClient slaClient = stdLib.getSlaClient();
                await();

                // ensure the value is correct
                actual = slaClient.getSlaFailureCount();

                // send one timestamp
                slaClient.checkTs(this.timestamp, "ash");
                counter.countDown();
            }
        }


        // these ensure the order of thread execution is what's desired:
        CountDownLatch noWait = new CountDownLatch(0);
        CountDownLatch latchOne = new CountDownLatch(1);
        CountDownLatch latchTwo = new CountDownLatch(1);
        CountDownLatch latchThree = new CountDownLatch(1);

        SingletonTestRunnable one = new SingletonTestRunnable(failureTS, 0, noWait, latchOne);
        SingletonTestRunnable two = new SingletonTestRunnable(successTS, 1, latchOne, latchTwo);
        SingletonTestRunnable three = new SingletonTestRunnable(failureTS, 0, latchTwo, latchThree);
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
        latchThree.await();

        assertEquals(one.expectedValue, one.actual);
        assertEquals(two.expectedValue, two.actual);
        assertEquals(three.expectedValue, three.actual);
    }
}
