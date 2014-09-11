package com.krux.server.http;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.Map;

public class StdHttpServerInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String, ChannelInboundHandlerAdapter> _httpHandlers;

    public StdHttpServerInitializer( Map<String, ChannelInboundHandlerAdapter> httpHandlers ) {
        _httpHandlers = httpHandlers;
    }

    @Override
    public void initChannel( SocketChannel ch ) throws Exception {
        ChannelPipeline p = ch.pipeline();

        // Uncomment the following line if you want HTTPS
        // SSLEngine engine =
        // SecureChatSslContextFactory.getServerContext().createSSLEngine();
        // engine.setUseClientMode(false);
        // p.addLast("ssl", new SslHandler(engine));

        p.addLast( "codec", new HttpServerCodec() );
        p.addLast( "aggregator", new HttpObjectAggregator( 1048576 ) );
        p.addLast( "handler", new StdHttpServerHandler( _httpHandlers ) );
    }
}