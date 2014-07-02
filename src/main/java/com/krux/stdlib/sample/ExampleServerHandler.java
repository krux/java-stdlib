package com.krux.stdlib.sample;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;

public class ExampleServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ExampleServerHandler.class.getName());

    private static final String CONTENT = "{'status':'ok','state':'" + KruxStdLib.APP_NAME + " is running.','class':'ExampleServerHandler'}";

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
        	log.info( "Here in the ExampleServerHandler");
            HttpRequest req = (HttpRequest) msg;
            
            boolean keepAlive = isKeepAlive(req);
            
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer( CONTENT.getBytes() ));
            res.headers().set(CONTENT_TYPE, "application/json");
            res.headers().set(CONTENT_LENGTH, res.content().readableBytes());
            if (!keepAlive) {
                ctx.write(res).addListener(ChannelFutureListener.CLOSE);
            } else {
                res.headers().set(CONNECTION, Values.KEEP_ALIVE);
                ctx.write(res);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error while processing request", cause);
        KruxStdLib.STATSD.count( "http.query.503" );
        ctx.close();
    }
}