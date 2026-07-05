package org.Amin.Contact.Exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ContactException extends RuntimeException{
    private final HttpResponseStatus status;

    public ContactException(HttpResponseStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }
}
