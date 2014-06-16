package com.krux.server.http;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpRequest;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;

public class StdHttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(StdHttpServerHandler.class.getName());
    
    private final static String STATUS_URL = "/__status";

    private static final String CONTENT = "{'status':'ok','state':'" + KruxStdLib.appName + " is running.'}";
    
    private Map<String, ChannelInboundHandlerAdapter> _httpHandlers;

    public StdHttpServerHandler(Map<String, ChannelInboundHandlerAdapter> httpHandlers) {
        _httpHandlers = httpHandlers;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
            long start = System.currentTimeMillis();

            HttpRequest req = (HttpRequest) msg;
            String uri = req.getUri();
            
            String[] parts = uri.split("\\?");
            String path = parts[0];
            log.info("path: " + path);
            
            if ( path.equals( STATUS_URL ) ) {
                
                if (is100ContinueExpected(req)) {
                    ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                }
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
                
            } else {
                
                ChannelInboundHandlerAdapter handler = _httpHandlers.get( path );
                if ( handler != null ) {
                    //pass control to submitted handler
                    ChannelPipeline p = ctx.pipeline();
                    p.addLast( handler );
                    p.remove( this );
                    
                } else {
                    // return 404
                    if (is100ContinueExpected(req)) {
                        ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                    }
                    boolean keepAlive = isKeepAlive(req);

                    FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
                    if (!keepAlive) {
                        ctx.write(res).addListener(ChannelFutureListener.CLOSE);
                    } else {
                        res.headers().set(CONNECTION, Values.KEEP_ALIVE);
                        ctx.write(res);
                    }
                    
                    KruxStdLib.statsd.count( "http.query.404" );
                }
            }

            long time = System.currentTimeMillis() - start;
            log.info("Request took " + time + "ms for whole request");
            KruxStdLib.statsd.time("http.query", time);

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error while processing request", cause);
        KruxStdLib.statsd.count( "http.query.503" );
        ctx.close();
    }
}