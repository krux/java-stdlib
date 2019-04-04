package com.krux.server.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;
import com.krux.stdlib.statsd.StatsdClient;
import com.krux.stdlib.status.StatusHandler;
import com.krux.stdlib.utils.SlaClient;

/**
 * An HTTP server that sends back the content of the received HTTP request in a
 * pretty plaintext form.
 */
public class StdHttpServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger( StdHttpServer.class.getName() );

    private int port;
    private KruxStdLib stdLib;

    public StdHttpServer(int port, KruxStdLib stdLib) {
        this.port = port;
        this.stdLib = stdLib;
    }

    public void run() {

        try {
            log.info( "Starting HTTP Server, listening on port " + port );

            // Configure the server.
            EventLoopGroup bossGroup = new NioEventLoopGroup( 1 );
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.option( ChannelOption.SO_BACKLOG, 1024 );
                b.group( bossGroup, workerGroup )
                    .channel( NioServerSocketChannel.class )
                    .childHandler( new StdHttpServerInitializer( stdLib ) );

                Channel ch = b.bind( port ).sync().channel();
                ch.closeFuture().sync();

            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch ( Exception e ) {
            log.error( "Cannot start HTTP server, shutting down", e );
            System.exit(1);
        }
    }
}