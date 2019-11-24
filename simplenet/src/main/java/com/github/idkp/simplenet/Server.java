package com.github.idkp.simplenet;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.function.Consumer;

public interface Server extends Closeable {
    int getPort();

    ConnectionReviewer getConnectionReviewer();

    boolean bind(int port) throws IOException;

    void unbind() throws IOException;

    boolean isBound();

    ActiveConnection getConnection(SocketAddress address);

    void closeConnection(ActiveConnection connection);

    Selector getSelector();

    ServerSocketChannel getChannel();

    void setConnectionAcceptAttemptHandler(Consumer<ConnectionAttemptResult> handler);

    void setErrorHandler(ServerErrorHandler handler);
}