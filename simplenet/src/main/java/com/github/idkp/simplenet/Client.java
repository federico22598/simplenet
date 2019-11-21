package com.github.idkp.simplenet;

import java.io.IOException;
import java.net.SocketAddress;

public interface Client {
    void connect(SocketAddress address, Runnable finishListener) throws IOException;

    ActiveConnection getConnection();

    boolean isConnected();
}
