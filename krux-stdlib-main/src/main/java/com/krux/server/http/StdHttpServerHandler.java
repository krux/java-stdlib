package com.krux.server.http;

import static io.netty.handler.codec.http.HttpUtil.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
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
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jr.ob.JSON;
import com.krux.stdlib.KruxStdLib;
import com.krux.stdlib.statsd.StatsdClient;
import com.krux.stdlib.status.AppState;
import com.krux.stdlib.status.StatusHandler;
import com.krux.stdlib.utils.SlaClient;

public class StdHttpServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger( StdHttpServerHandler.class );
    private final static String STATUS_URL = "__status";
    private final static String SLA_URL = "__sla";

    private final KruxStdLib stdLib;
    private final StatsdClient statsd;
    private final SlaClient slaClient;
    private final StatusHandler statusHandler;

    private AppState stateCode = AppState.OK;
    private AppState failureCode = AppState.FAILURE;

    private Map<String, Object> applicationState = Collections.synchronizedMap( new HashMap<String, Object>() );
    private Map<String, Object> applicationSlaState = Collections.synchronizedMap( new HashMap<String, Object>() );
    private Map<String, Object> applicationSlaFailureState = Collections.synchronizedMap( new HashMap<String, Object>() );


    private Map<String, ChannelInboundHandlerAdapter> _httpHandlers;

    public StdHttpServerHandler(
            KruxStdLib stdLib, 
            StatsdClient statsd, 
            StatusHandler statusHandler,
            SlaClient slaClient, 
            Map<String, ChannelInboundHandlerAdapter> httpHandlers
        ) {
        this.stdLib = stdLib;
        this.statusHandler = statusHandler;
        this.slaClient = slaClient;
        this.statsd = statsd;
        _httpHandlers = httpHandlers;
        resetAllStates();
    }

    protected void resetApplicationState() {
        applicationState.put( StatusKeys.state.toString(), stateCode.toString() );
        applicationState.put( StatusKeys.status.toString(), getNominalStatusMessage() );
        applicationState.put( StatusKeys.version.toString(), stdLib.getAppVersion() );
    }

    protected void resetSlaState() {
        applicationSlaState.put( StatusKeys.state.toString(), stateCode.toString() );
        applicationSlaState.put( StatusKeys.status.toString(), getNominalSlaMessage() );
        applicationSlaState.put( StatusKeys.version.toString(), stdLib.getAppVersion() );

        applicationSlaFailureState.put( StatusKeys.state.toString(), failureCode.toString() );
        applicationSlaFailureState.put( StatusKeys.status.toString(), getFailureSlaMessage(0) );
        applicationSlaFailureState.put( StatusKeys.version.toString(), stdLib.getAppVersion() );
    }

    protected void resetAllStates() {
        resetApplicationState();
        resetSlaState();
    }

    protected String getNominalStatusMessage() {
        return stdLib.getAppName() + " is running nominally";
    }

    protected String getNominalSlaMessage() {
        return stdLib.getAppName() + " is meeting its SLA of " + stdLib.getSlaInSeconds() + " seconds.";
    }

    protected String getFailureSlaMessage(long failureCount) {
        return stdLib.getAppName() + " is not meeting its SLA of " + stdLib.getSlaInSeconds() + " seconds (" + failureCount + ").";
    }

    protected String getNotFoundBody() {
        return
                "<html>"
                + "<head><title>404 Not Found</title></head> "
                + "<body bgcolor=\"white\"> "
                + "<center><h1>404 Not Found</h1></center> <hr><center>Krux - " + stdLib.getAppName() + "</center> "
                + "</body> "
                + "</html>";
    }

    @Override
    public void channelReadComplete( ChannelHandlerContext ctx ) {
        // ctx.flush();
    }

    @Override
    public void channelRead( ChannelHandlerContext ctx, Object msg ) throws Exception {

        if ( msg instanceof HttpRequest ) {
            long start = System.currentTimeMillis();

            HttpRequest req = (HttpRequest) msg;
            String uri = req.uri();

            String[] parts = uri.split( "\\?" );
            String path = parts[0];
            log.info( "path: " + path );

            if ( is100ContinueExpected( req ) ) {
                ctx.write( new DefaultFullHttpResponse( HTTP_1_1, CONTINUE ) );
            }
            boolean keepAlive = isKeepAlive( req );

            if ( path.trim().endsWith(STATUS_URL) ) {
                String message = JSON.std.asString(statusHandler.getStatus());
                log.debug("responding to {} with {}", STATUS_URL, message);
                sendJson(message, ctx, keepAlive);
            } else if ( path.trim().endsWith( SLA_URL ) ) {

                String message;
                // if sla is met return OK message and return failure if not
                long slaFailures = slaClient.getSlaFailureCount();
                if (slaFailures == 0) {
                    message = JSON.std.asString(applicationSlaState);
                } else {
                    applicationSlaFailureState.put( StatusKeys.status.toString(), getFailureSlaMessage(slaFailures));
                    message = JSON.std.asString(applicationSlaFailureState);
                }

                log.debug("responding to {} with {}", SLA_URL, message);
                sendJson(message, ctx, keepAlive);
            } else {

                ChannelInboundHandlerAdapter handler = _httpHandlers.get( path );
                if ( handler != null ) {
                    passToHandler( ctx, msg, handler );

                } else {
                    // use default handler if configured
                    handler = _httpHandlers.get( "__default" );

                    if ( handler != null ) {
                        passToHandler( ctx, msg, handler );
                    } else {
                        log.info( "No configured URL, returning 404" );
                        sendMessage(getNotFoundBody(), ctx, keepAlive, NOT_FOUND, "text/html");
                        statsd.count( stdLib.getAppName() + "_HTTP_404" );
                    }
                }
            }

            ReferenceCountUtil.release( msg );
            long time = System.currentTimeMillis() - start;
            log.info( "Request took " + time + "ms for whole request" );
            statsd.time( stdLib.getAppName() + "_HTTP_200", time );
        }
    }

    protected void sendJson(String message, ChannelHandlerContext context, boolean keepAlive) throws Exception {
        sendMessage(message, context, keepAlive, OK, "application/json");
    }

    protected void sendMessage(
            String message, 
            ChannelHandlerContext context, 
            boolean keepAlive,
            HttpResponseStatus httpStatus,
            String contentType
        ) throws Exception {

        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, httpStatus, Unpooled.wrappedBuffer( message.getBytes("UTF-8")));
        res.headers().set( CONTENT_TYPE, contentType );
        res.headers().set( CONTENT_LENGTH, res.content().readableBytes() );
        if ( !keepAlive ) {
            context.writeAndFlush( res ).addListener( ChannelFutureListener.CLOSE );
        } else {
            res.headers().set( CONNECTION, HttpHeaderValues.KEEP_ALIVE );
            context.writeAndFlush( res );
        }
    }

    private void passToHandler( ChannelHandlerContext ctx, Object msg, ChannelInboundHandlerAdapter handler )
            throws InstantiationException, IllegalAccessException {
        // pass control to submitted handler
        log.info( "Found handler" );
        ChannelPipeline p = ctx.pipeline();
        try {
            p.remove( "final_handler" );
        } catch ( Exception e ) {
        }
        p.addLast( "final_handler", handler.getClass().newInstance() );

        // is this really the best way?
        ctx.fireChannelRead( msg );
        ctx.fireChannelReadComplete();
    }

    @Override
    public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception {
        log.error( "Error while processing request", cause );
        statsd.count( stdLib.getAppName() + "_HTTP_503" );
        ctx.close();
    }

}
