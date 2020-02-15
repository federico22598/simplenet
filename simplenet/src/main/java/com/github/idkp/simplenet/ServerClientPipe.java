package com.github.idkp.simplenet;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ServerClientPipe extends Closeable {
    void open(SocketChannel socketChannel, StandardServerClient client) throws IOException;

    @Override
    void close() throws IOException;
}
