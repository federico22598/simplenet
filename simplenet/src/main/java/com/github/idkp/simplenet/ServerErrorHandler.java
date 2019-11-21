package com.github.idkp.simplenet;

public interface ServerErrorHandler {
    void handle(String name, Server server);

    void handle(String name, Server server, Exception e);

    void handle(String name, Server server, ActiveConnection connection);

    void handle(String name, Server server, ActiveConnection connection, Exception e);
    /*void handleWriteIOException(Server server, ActiveConnection connection, IOException e);

    void handleReadIOException(Server server, ActiveConnection connection, IOException e);

    void handleEOF(Server server, ActiveConnection connection);*/
}
