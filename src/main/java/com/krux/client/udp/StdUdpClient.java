package com.krux.client.udp;

import com.krux.stdlib.KruxStdLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Prashanth Jonnalagadda
 */
public final class StdUdpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(StdUdpClient.class);
    private static final int UDP_SENDER_THREADS = 1;
    private static final int QUEUE_SIZE = 1000;
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final Charset CHAR_ENCODING = Charset.forName("UTF-8");
    private final DatagramSocket _clientSocket;
    private InetAddress _host;
    private int _port;
    private ThreadPoolExecutor _executor;

    public StdUdpClient(String hostname, int port) throws SocketException, UnknownHostException {

        _host = InetAddress.getByName(hostname);
        _port = port;

        _clientSocket = new DatagramSocket();
        _clientSocket.setSendBufferSize(BUFFER_SIZE);
        _clientSocket.setReceiveBufferSize(BUFFER_SIZE);
        _clientSocket.connect(_host, _port);

        _executor = new ThreadPoolExecutor(UDP_SENDER_THREADS, UDP_SENDER_THREADS,
                Long.MAX_VALUE, TimeUnit.NANOSECONDS,
                new ArrayBlockingQueue<Runnable>(QUEUE_SIZE),
                new ThreadFactory() {
                    final ThreadFactory delegate = Executors.defaultThreadFactory();
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread result = delegate.newThread(r);
                        result.setName("UdpClient-" + result.getName());
                        result.setDaemon(true);
                        return result;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    public void shutdown() {
        LOGGER.info("UDP client Shutting down ...");

        _executor.shutdown(); // Disable new tasks from being submitted

        try {
            while (true) {
                // Wait a while for existing tasks to terminate
                if (!_executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    LOGGER.debug("Waiting for UDP client to shutdown ...");
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("UDP client shutdown failed", e);
        }
        finally {
            _clientSocket.close();
        }
    }

    public void send(final String message) {

        _executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final byte[] sendData = message.getBytes(CHAR_ENCODING);
                    long start = System.currentTimeMillis();
                    _clientSocket.send(new DatagramPacket(sendData, sendData.length, _host, _port));
                    long end = System.currentTimeMillis();
                    KruxStdLib.STATSD.time("udp_send", end - start);
                } catch (Exception e) {
                    LOGGER.error("Failed to send UDP packet", e);
                    KruxStdLib.STATSD.count("udp_failed");
                }
            }
        });

    }
}