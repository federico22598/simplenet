package com.github.idkp.simplenet;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ServerClientPipeline extends Closeable {
    void openPipe(String name, ServerClientPipe pipe, SocketChannel clientChannel) throws IOException;

    void closePipe(String name) throws IOException;

    void closePipe(ServerClientPipe pipe) throws IOException;

    ServerClientPipe getPipe(String name);

    int getPipeCount();

    ServerClient getClient();
}
