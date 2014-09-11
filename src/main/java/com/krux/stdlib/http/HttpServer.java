package com.krux.stdlib.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HTTP server that sends back the content of the received HTTP request in a
 * pretty plaintext form.
 */
public class HttpServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger( HttpServer.class.getName() );

    private int port;

    public void run() {

        try {
            port = Integer.parseInt( System.getProperty( "bitset.http.server.port", "80" ) );
            log.info( "Starting bitset HTTP QUERY service, listening on port " + port );

            // Configure the server.
            EventLoopGroup bossGroup = new NioEventLoopGroup( 1 );
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.option( ChannelOption.SO_BACKLOG, 1024 );
                b.group( bossGroup, workerGroup ).channel( NioServerSocketChannel.class )
                        .childHandler( new HttpServerInitializer() );

                Channel ch = b.bind( port ).sync().channel();
                ch.closeFuture().sync();
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch ( Exception e ) {
            log.error( "HTTP server failed", e );
        }
    }
}