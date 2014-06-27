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
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;

public class StdHttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(StdHttpServerHandler.class.getName());
    
    private final static String STATUS_URL = "/__status";
    
    private static HttpResponseStatus statusCode = HttpResponseStatus.OK;
    private static String statusResponseMessage = KruxStdLib.appName + " is running nominally";

    private static final String BODY_404 = "<html> <head><title>404 Not Found</title></head> <body bgcolor=\"white\"> <center><h1>404 Not Found</h1></center> <hr><center>Krux - " + KruxStdLib.appName + "</center> </body> </html>";
    
    private Map<String, ChannelInboundHandlerAdapter> _httpHandlers;

    public StdHttpServerHandler(Map<String, ChannelInboundHandlerAdapter> httpHandlers) {
        _httpHandlers = httpHandlers;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        //ctx.flush();
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
            
            if (is100ContinueExpected(req)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
            boolean keepAlive = isKeepAlive(req);
            
            if ( path.equals( STATUS_URL ) ) {
                FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer( 
                        ("{'status':" + statusCode.code() + ",'message':'" + statusResponseMessage + "'}").getBytes() ));
                res.headers().set(CONTENT_TYPE, "application/json");
                res.headers().set(CONTENT_LENGTH, res.content().readableBytes());
                if (!keepAlive) {
                    ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
                } else {
                    res.headers().set(CONNECTION, Values.KEEP_ALIVE);
                    ctx.writeAndFlush(res);
                }
                
            } else {
                
                ChannelInboundHandlerAdapter handler = _httpHandlers.get( path );
                if ( handler != null ) {
                    //pass control to submitted handler
                	log.info( "Found handler" );
                    ChannelPipeline p = ctx.pipeline();
                    p.addLast( "final_handler", handler.getClass().newInstance() );
                    
                    //is this really the best way?
                    ctx.fireChannelRead(msg);
                    ctx.fireChannelReadComplete();
                    
                } else {

                	log.info( "No configured URL, returning 404" );
                    FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND, Unpooled.wrappedBuffer( BODY_404.getBytes( ) ) );
                    res.headers().set(CONTENT_TYPE, "text/html");
                    res.headers().set(CONTENT_LENGTH, res.content().readableBytes());
                    if (!keepAlive) {
                        ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
                    } else {
                        res.headers().set(CONNECTION, Values.KEEP_ALIVE);
                        ctx.writeAndFlush(res);
                    }
                    
                    KruxStdLib.statsd.count( "http.query.404" );
                }
            }
            
            ReferenceCountUtil.release(msg);
            long time = System.currentTimeMillis() - start;
            log.info("Request took " + time + "ms for whole request");
            KruxStdLib.statsd.time("http.query.200", time);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error while processing request", cause);
        KruxStdLib.statsd.count( "http.query.503" );
        ctx.close();
    }
    
    public void setStatusCodeAndMessage( HttpResponseStatus code, String message ) {
        statusCode = code;
        statusResponseMessage = message;
    }
}