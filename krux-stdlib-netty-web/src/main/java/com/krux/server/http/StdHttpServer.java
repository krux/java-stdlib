package com.krux.server.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.AppState;
import com.krux.stdlib.http.server.HttpService;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * An HTTP server that sends back the content of the received HTTP request in a
 * pretty plaintext form.
 */
public class StdHttpServer implements HttpService, Runnable {

    private static final Logger log = LoggerFactory.getLogger(StdHttpServer.class.getName());

    private int _port;
    private Map<String, ChannelInboundHandlerAdapter> _httpHandlers;
    private Thread _t;

    private StdHttpServer(int port, Map<String, ChannelInboundHandlerAdapter> httpHandlers) {
        _port = port;
        _httpHandlers = httpHandlers;
    }
    
    public StdHttpServer(){
    }

    public void run() {

        try {
            log.info("Starting HTTP Server, listening on port " + _port);

            // Configure the server.
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.option(ChannelOption.SO_BACKLOG, 1024);
                b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                        .childHandler(new StdHttpServerInitializer(_httpHandlers));

                Channel ch = b.bind(_port).sync().channel();
                ch.closeFuture().sync();

            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            log.error("Cannot start HTTP server, shutting down", e);
            System.exit(1);
        }
    }

    @Override
    public void start() {
        _t = new Thread(this);
        _t.start();      
    }

    @Override
    public void stop() {
        _t.interrupt();
    }

    @Override
    public void setStatusCodeAndMessage(AppState state, String message) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void resetStatusCodeAndMessageOK() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addAdditionalStatus(String key, Object value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void registerHttpHandler(String url, ChannelInboundHandlerAdapter handler) {
        // TODO Auto-generated method stub
        
    }
}