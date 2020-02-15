package com.github.idkp.simplenet;

public interface ServerErrorHandler {
    void handle(Server server, String errorName, Exception e);
}
