package com.github.idkp.simplenet;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;

public interface Client extends Closeable {
    void connect(ServerConnectionConfiguration configuration, Runnable finishListener) throws IOException;

    ActiveConnection getConnection();

    boolean isConnected();
}
