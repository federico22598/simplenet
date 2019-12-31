package com.github.idkp.simplenet;

import java.io.Closeable;
import java.nio.channels.SocketChannel;
import java.util.function.Supplier;

public interface ActiveConnection extends Closeable {
    <T> void sendPacket(String name, T payload);

    void sendPacket(String name);

    boolean isReadingPacketData();

    ConnectionConfiguration getConfiguration();

    SocketChannel getChannel();
}
