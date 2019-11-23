package com.github.idkp.simplenet;

import java.io.Closeable;
import java.nio.channels.SocketChannel;
import java.util.function.Supplier;

public interface ActiveConnection extends Closeable {
    <T> void sendPacket(String name, T payload);

    void sendPacket(String name);

    void registerPayloadlessPacket(String name);

    void setPacketPayloadEncoder(String packetName, PayloadEncoder<?> encoder);

    void setPacketPayloadDecoder(String packetName, PayloadDecoder<?> decoder);

    void setPacketPayloadFactory(String packetName, Supplier<?> factory);

    <T> boolean addPacketReceiveListener(String packetName, String name, PacketReceiveListener<T> listener);

    boolean removePacketReceiveListener(String packetName, String name);

    boolean removePacketReceiveListeners(String packetName);

    boolean hasPacketReceiveListener(String packetName, String name);

    boolean hasPacketReceiveListeners(String packetName);

    SocketChannel getChannel();
}
