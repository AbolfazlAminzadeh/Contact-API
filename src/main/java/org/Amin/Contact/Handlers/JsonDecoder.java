package org.Amin.Contact.Handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.Amin.Contact.Contact.Contact;
import org.Amin.Contact.Exceptions.ContactException;
import org.Amin.Contact.Exceptions.InvalidPhoneNumber;

import java.nio.charset.Charset;
import java.util.List;

import static org.Amin.Contact.Util.Util.mapper;

public class JsonDecoder extends MessageToMessageDecoder<FullHttpRequest> {
    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, List<Object> out) throws ContactException {
        if (!msg.decoderResult().isSuccess()) throw new ContactException(HttpResponseStatus.BAD_REQUEST,"Bad/Wrong HTTP Request");
        String uri = msg.uri();
        switch (msg.method().name()) {
            case "POST","PUT" -> {
                if (uri.equals("/contacts")) {
                    String json = msg.content().toString(Charset.defaultCharset());
                    try {
                        Contact contact = mapper.readValue(json,Contact.class);
                        contact.validate();
                        out.add(contact);
                    } catch (InvalidPhoneNumber | IllegalArgumentException e) {
                        throw new ContactException(HttpResponseStatus.UNPROCESSABLE_ENTITY,e.getMessage());
                    } catch (Exception e) {
                        throw new ContactException(HttpResponseStatus.UNPROCESSABLE_ENTITY,"Invalid JSON Body, please take a look at your json output");
                    }
                }
                else out.add(msg.retain());
            }
            default -> out.add(msg.retain());
        }
    }

}
