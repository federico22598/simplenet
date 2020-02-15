package com.github.idkp.simplenet;

import java.io.Closeable;
import java.io.IOException;

public interface Server extends Closeable {
    int getPort();

    boolean bind(int port) throws IOException;

    boolean isBound();

    ServerClient getClient(ClientID id);
}