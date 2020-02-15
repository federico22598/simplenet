package com.github.idkp.simplenet;

public interface ClientPipeErrorHandler {
    void handle(ClientPipe pipe, String errorName);

    void handle(ClientPipe pipe, String errorName, Exception e);
}
