package com.krux.stdlib.status;

import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.statsd.StatsdClient;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpHeaderValues;

public class StatusHandlerWrapper extends ChannelInboundHandlerAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger(StatusHandlerWrapper.class);

    private StatusHandler handler;
    private StatsdClient statsd;

    public StatusHandlerWrapper(StatusHandler handler, StatsdClient statsd) {
        this.handler = handler;
        this.statsd = statsd;
    }

    @Override
    public void channelReadComplete( ChannelHandlerContext ctx ) {
        ctx.flush();
    }

    @Override
    public void channelRead( ChannelHandlerContext ctx, Object msg ) throws Exception {

        if ( msg instanceof HttpRequest ) {
            HttpRequest req = (HttpRequest) msg;

            boolean keepAlive = isKeepAlive( req );

            String status = handler.getEncodedStatus();
            FullHttpResponse res = new DefaultFullHttpResponse( HTTP_1_1, OK, Unpooled.wrappedBuffer( status.getBytes("UTF-8") ) );
            res.headers().set( CONTENT_TYPE, "application/json" );
            res.headers().set( CONTENT_LENGTH, res.content().readableBytes() );
            if ( !keepAlive ) {
                ctx.write( res ).addListener( ChannelFutureListener.CLOSE );
            } else {
                res.headers().set( CONNECTION, HttpHeaderValues.KEEP_ALIVE );
                ctx.write( res );
            }
        }
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception {
        LOGGER.error( "Error while processing request", cause );
        statsd.count( "http.query.503" );
        ctx.close();
    }
}
