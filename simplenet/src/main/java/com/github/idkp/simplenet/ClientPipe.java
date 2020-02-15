package com.github.idkp.simplenet;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ClientPipe extends Closeable {
    void open(SocketChannel socketChannel) throws IOException;

    @Override
    void close() throws IOException;
}
