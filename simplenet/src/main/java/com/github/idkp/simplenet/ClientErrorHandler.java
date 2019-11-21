package com.github.idkp.simplenet;

public interface ClientErrorHandler {
    void handle(String name, Client client);

    void handle(String name, Client client, Exception e);

    /*void handleWriteIOException(Client client, IOException e);

    void handleReadIOException(Client client, IOException e);

    void handleEOF(Client client);*/
}
