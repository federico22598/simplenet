package com.github.idkp.simplenet;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Set;

public interface ClientPipeline extends Closeable {
    void openPipe(ClientPipe pipe, SocketChannel pipeChannel, SocketAddress serverAddress) throws IOException;

    void closePipe(ClientPipe pipe) throws IOException;

    Set<ClientPipe> getPipes();
}
