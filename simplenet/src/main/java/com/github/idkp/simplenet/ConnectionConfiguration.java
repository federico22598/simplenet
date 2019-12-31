package com.github.idkp.simplenet;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public interface ConnectionConfiguration {
    void registerPacket(short id, String name);

    Short getPacketId(String name);

    void setPacketPayloadEncoder(String packetName, PayloadEncoder<?> encoder);

    PayloadEncoder<?> getPacketPayloadEncoder(String packetName);

    PayloadEncoder<?> getPacketPayloadEncoder(short packetId);

    void setPacketPayloadDecoder(String packetName, PayloadDecoder<?> decoder);

    PayloadDecoder<?> getPacketPayloadDecoder(String packetName);

    PayloadDecoder<?> getPacketPayloadDecoder(short packetId);

    void setPacketPayloadFactory(String packetName, Supplier<?> factory);

    Supplier<?> getPacketPayloadFactory(String packetName);

    Supplier<?> getPacketPayloadFactory(short packetId);

    <T> void addPacketListener(String packetName, String name, PacketListener<T> listener);

    void removePacketListener(String packetName, String name);

    void removePacketListeners(String packetName);

    boolean hasPacketListener(String packetName, String name);

    boolean hasPacketListeners(String packetName);

    Collection<PacketListener<?>> getPacketListeners(String packetName);

    Collection<PacketListener<?>> getPacketListeners(short packetId);
}
