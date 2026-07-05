package org.Amin.Contact.Handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.http.*;
import org.Amin.Contact.Contact.Contact;
import org.Amin.Contact.Exceptions.ContactException;
import org.Amin.Contact.Server;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static org.Amin.Contact.Util.Util.mapper;

public class MainHandler extends SimpleChannelInboundHandler<Object> {

    private final static ConcurrentHashMap<Integer, Contact> DB = new ConcurrentHashMap<>(1<<6); //64 for init

    private final Server server;

    public MainHandler(Server server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) throws Exception {
        switch (object) {
            case Contact contact -> handleCreate(ctx,contact);
            case FullHttpRequest request -> handleRequest(ctx, request);
            default -> throw new ContactException(NOT_FOUND,"The page you looking for, not found");
        }
    }

    private void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws ContactException, JsonProcessingException {
        if (request.uri().startsWith("/shutdown")) {
            if (server.shuttingDown.compareAndSet(false,true)) {
                ctx.channel().setOption(ChannelOption.AUTO_READ,false);
                response(ctx, OK, "{\"status\":\"shutting down\"}", false);
                server.shutdown();
            }
        }
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        String uri = request.uri();
        switch (request.method().name()) {
            case "GET" -> {
                if (uri.equals("/contacts")) handleDisplay(ctx,keepAlive);
                else throw new ContactException(NOT_FOUND,"The page you looking for, not found");
            }

            case "DELETE" -> {
                if (uri.startsWith("/contacts/")) handleDelete(ctx,uri.substring(10),keepAlive);
                else throw new ContactException(NOT_FOUND,"The page you looking for, not found");
            }
        }
    }

    private void handleCreate(ChannelHandlerContext ctx, Contact contact) throws JsonProcessingException, ContactException {
        DB.put(contact.id(),contact);
        response(ctx,CREATED, mapper.writeValueAsString(contact) ,false);
    }

    private void handleDelete(ChannelHandlerContext ctx, String id, boolean keepalive) throws ContactException{
        if (!id.matches("\\d+")) throw new ContactException(UNPROCESSABLE_ENTITY,"Invalid Identifier, check ID");
        if (DB.remove(Integer.parseInt(id)) == null) throw new ContactException(NOT_FOUND,"No Record Find with that ID");
        response(ctx,OK, "{\"message\":\"Deleted Successfully\"}",keepalive);
    }

    private void handleDisplay(ChannelHandlerContext ctx, boolean keepalive) throws JsonProcessingException {
        response(ctx,OK,mapper.writeValueAsString(DB.values()),keepalive);
    }

    private void response(ChannelHandlerContext ctx,HttpResponseStatus status, CharSequence body,boolean keepAlive) {
        ByteBuf buf = Unpooled.copiedBuffer(body, Charset.defaultCharset());
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,status,buf);
        response.headers().set(CONTENT_TYPE,APPLICATION_JSON);
        response.headers().set(CONTENT_LENGTH,buf.readableBytes());
        response.headers().set(CONNECTION,KEEP_ALIVE);
        if (keepAlive) ctx.writeAndFlush(response); // Keep It Alive :)
        else ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        if (throwable instanceof DecoderException _) throwable = throwable.getCause();
        if (throwable instanceof ContactException e) {
            String json = "{\"error\":\"" + e.getMessage() + "\"}";
            response(ctx, e.getStatus(), json, false);
        } else {
            System.out.println("Take a look: " + throwable);
        }
    }
}
