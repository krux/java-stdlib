package com.krux.stdlib.sample;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;

public class ExceptionServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ExceptionServerHandler.class.getName());

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
        	log.info( "Here in the ExceptionServerHandler");
            throw new Exception( "test exception" );
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error while processing request", cause);
        KruxStdLib.statsd.count( "http.query.503" );
        ctx.close();
    }
}