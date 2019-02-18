package io.github.splotycode.mosaik.webapi.server.netty;

import io.github.splotycode.mosaik.util.ExceptionUtil;
import io.github.splotycode.mosaik.webapi.config.WebConfig;
import io.github.splotycode.mosaik.webapi.request.Request;
import io.github.splotycode.mosaik.webapi.response.CookieKey;
import io.github.splotycode.mosaik.webapi.response.Response;
import io.github.splotycode.mosaik.webapi.server.BadRequestException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ssl.NotSslRecordException;

import java.util.Map;

public class WebServerHandler extends SimpleChannelInboundHandler {

    private NettyWebServer server;

    public WebServerHandler(NettyWebServer server) {
        this.server = server;
    }

    private FullHttpRequest originalRequest = null;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest nettyRequest = (FullHttpRequest) msg;
            originalRequest = nettyRequest;
            if (!nettyRequest.decoderResult().isSuccess()) {
                throw new BadRequestException("Netty Decoder Failed");
            }
            Request request = new NettyRequest(server, nettyRequest, ctx);

            long start = System.currentTimeMillis();
            Response response = server.handleRequest(request);
            response.finish(request, server);
            server.addTotalTime(System.currentTimeMillis() - start);

            ByteBuf content = Unpooled.buffer(response.getRawContent().available());
            content.writeBytes(response.getRawContent(), response.getRawContent().available());

            DefaultFullHttpResponse nettyResponse = new DefaultFullHttpResponse(
                    NettyUtils.convertHttpVersion(response.getHttpVersion()),
                    HttpResponseStatus.valueOf(response.getResponseCode()),
                    content
            );
            for (Map.Entry<String, String> pair : response.getHeaders().entrySet()) {
                nettyResponse.headers().set(pair.getKey(), pair.getValue());
            }
            for (Map.Entry<CookieKey, String> cookie : response.getSetCookies().entrySet()) {
                nettyResponse.headers().add(HttpHeaderNames.SET_COOKIE, cookie.getKey().toHeaderString(cookie.getValue()));
            }
            ChannelFuture future = ctx.writeAndFlush(nettyResponse);
            if (!request.isKeepAlive()) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (server.isSsl() &&
                server.getConfig().getDataDefault(WebConfig.FORCE_HTTPS, false) &&
            ExceptionUtil.instanceOfCause(cause, NotSslRecordException.class)) {
            DefaultFullHttpResponse req = new DefaultFullHttpResponse(
                    originalRequest.protocolVersion(),
                    HttpResponseStatus.PERMANENT_REDIRECT
            );
            req.headers().set(HttpHeaderNames.LOCATION, originalRequest.uri());
            ctx.writeAndFlush(req).addListener(ChannelFutureListener.CLOSE);
        }
        Response response = server.getErrorHandler().handleError(cause);
        response.finish(null, server);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(response.getRawContent(), response.getRawContent().available());
        DefaultFullHttpResponse nettyResponse = new DefaultFullHttpResponse(
                NettyUtils.convertHttpVersion(response.getHttpVersion()),
                HttpResponseStatus.valueOf(response.getResponseCode()),
                byteBuf
        );
        for (Map.Entry<String, String> pair : response.getHeaders().entrySet()) {
            nettyResponse.headers().set(pair.getKey(), pair.getValue());
        }
        for (Map.Entry<CookieKey, String> cookie : response.getSetCookies().entrySet()) {
            nettyResponse.headers().add(HttpHeaderNames.SET_COOKIE, cookie.getKey().toHeaderString(cookie.getValue()));
        }
        ctx.writeAndFlush(nettyResponse).addListener(ChannelFutureListener.CLOSE);
    }
}
