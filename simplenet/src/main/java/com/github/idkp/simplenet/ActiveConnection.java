package com.github.idkp.simplenet;

import java.io.Closeable;
import java.nio.channels.SocketChannel;
import java.util.function.Supplier;

public interface ActiveConnection extends Closeable {
    <T> void sendPacket(String name, T payload);

    void sendPacket(String name);

    boolean isReadingPacketData();

    void registerPayloadlessPacket(String name);

    void setPacketPayloadEncoder(String packetName, PayloadEncoder<?> encoder);

    void setPacketPayloadDecoder(String packetName, PayloadDecoder<?> decoder);

    void setPacketPayloadFactory(String packetName, Supplier<?> factory);

    <T> boolean addPacketListener(String packetName, String name, PacketListener<T> listener);

    boolean removePacketListener(String packetName, String name);

    boolean removePacketListeners(String packetName);

    boolean hasPacketListener(String packetName, String name);

    boolean hasPacketListeners(String packetName);

    SocketChannel getChannel();
}
