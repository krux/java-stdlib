package com.krux.stdlib.http;

import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

public class HttpDefaultStatusHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger( HttpDefaultStatusHandler.class.getName() );

    private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd', '!', '\n' };

    final private static MetricRegistry metrics = new MetricRegistry();

    @Override
    public void channelReadComplete( ChannelHandlerContext ctx ) {
        ctx.flush();
    }

    @Override
    public void channelRead( ChannelHandlerContext ctx, Object msg ) throws Exception {

        if ( msg instanceof HttpRequest ) {
            long start = System.currentTimeMillis();

            HttpRequest req = (HttpRequest) msg;

            // basic parts
            String uri = req.getUri();
            log.info( "uri: " + uri );

            String response = null;

            // ignore browser silliness
            if ( !uri.equals( "/favicon.ico" ) ) {

                String[] parts = uri.split( "\\?" );
                String path = parts[0];
                log.info( "path: " + path );

                String[] pathParts = path.split( "\\/" );
                String reqType = pathParts[1];
                log.info( "reqType: " + reqType );

                // querystring
                Map<String, String> params = new HashMap<String, String>();
                if ( parts.length > 1 ) {
                    String qs = parts[1];
                    log.info( "qs: " + qs );
                    String[] qsParams = qs.split( "\\&" );
                    for ( String paramPair : qsParams ) {
                        String[] nameValue = paramPair.split( "=" );
                        if ( nameValue.length == 2 ) {
                            params.put( nameValue[0], nameValue[1] );
                            log.info( "\tnv: " + nameValue[0] + " : " + nameValue[1] );
                        } else {
                            params.put( nameValue[0], nameValue[0] );
                            log.info( "\tnv: " + nameValue[0] + " : " + nameValue[0] );
                        }
                    }
                }

                String publisherId = pathParts[2];
                log.info( "publisherId: " + publisherId );

                // if (reqType.equals("campaigns")) {
                // // logic for Wanderely here
                // log.info("Will get campaign info");
                // response = CampaignResponseBuider.getResponse(publisherId,
                // params);
                // } else if (reqType.equals("funnels")) {
                // // logic for funnels here
                // log.info("Will get funnel info");
                // response =
                // EventsFunnelsResponseBuider.getResponse(publisherId, params);
                // }

                // ignore
            }

            if ( is100ContinueExpected( req ) ) {
                ctx.write( new DefaultFullHttpResponse( HTTP_1_1, CONTINUE ) );
            }
            boolean keepAlive = isKeepAlive( req );

            if ( response != null ) {
                FullHttpResponse res = new DefaultFullHttpResponse( HTTP_1_1, OK, Unpooled.wrappedBuffer( response
                        .getBytes() ) );
                res.headers().set( CONTENT_TYPE, "application/json" );
                res.headers().set( CONTENT_LENGTH, res.content().readableBytes() );
                if ( !keepAlive ) {
                    ctx.write( res ).addListener( ChannelFutureListener.CLOSE );
                } else {
                    res.headers().set( CONNECTION, Values.KEEP_ALIVE );
                    ctx.write( res );
                }
            } else {
                FullHttpResponse res = new DefaultFullHttpResponse( HTTP_1_1, OK, Unpooled.wrappedBuffer( CONTENT ) );
                res.headers().set( CONTENT_TYPE, "plain/text" );
                res.headers().set( CONTENT_LENGTH, res.content().readableBytes() );
                if ( !keepAlive ) {
                    ctx.write( res ).addListener( ChannelFutureListener.CLOSE );
                } else {
                    res.headers().set( CONNECTION, Values.KEEP_ALIVE );
                    ctx.write( res );
                }
            }

            long time = System.currentTimeMillis() - start;
            log.info( "Request took " + time + "ms for whole request" );

        }
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception {
        log.error( "Error while processing request", cause );
        ctx.close();
    }
}