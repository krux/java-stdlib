package com.krux.server.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import com.krux.stdlib.KruxStdLib;
import com.krux.stdlib.statsd.StatsdClient;
import com.krux.stdlib.status.StatusHandler;
import com.krux.stdlib.utils.SlaClient;

public class StdHttpServerInitializer extends ChannelInitializer<SocketChannel> {

    private KruxStdLib stdLib;
    private StatsdClient statsd;
    private SlaClient slaClient;
    private StatusHandler statusHandler;

    public StdHttpServerInitializer(KruxStdLib stdLib) {
        this(stdLib, stdLib.getStatsdClient(), stdLib.getSlaClient(), stdLib.getStatusHandler());
    }

    public StdHttpServerInitializer(
            KruxStdLib stdLib,
            StatsdClient statsd,
            SlaClient slaClient,
            StatusHandler statusHandler
        ) {
        this.stdLib = stdLib;
        this.statsd = statsd;
        this.slaClient = slaClient;
        this.statusHandler = statusHandler;
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
        p.addLast( "handler", new StdHttpServerHandler(stdLib, statsd, statusHandler, slaClient, stdLib.getHttpHandlers()) );
    }
}