package org.Amin.Contact.Handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.Amin.Contact.Server;

import java.nio.charset.Charset;

public class TestHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private final static ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Test Example Response", Charset.defaultCharset()));
    private final Server server;


    public TestHandler(Server server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        if (request.uri().startsWith("/shutdown")) {
            server.shutdown();
            return;
        }
        FullHttpResponse req = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf.duplicate());
        req.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
        req.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
        if (HttpUtil.isKeepAlive(request)) {
            req.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(req);
        } else ctx.writeAndFlush(req).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        // Ignore
    }
}
