package com.github.idkp.simplenet.packet;

import java.util.Collection;

public interface PacketPipeInputConfiguration {
    void registerPacket(short id, String name);

    Short getPacketId(String name);

    void setPacketPayloadDecoder(String packetName, PayloadDecoder<?> decoder);

    PayloadDecoder<?> getPacketPayloadDecoder(String packetName);

    PayloadDecoder<?> getPacketPayloadDecoder(short packetId);

    <T> void addPacketListener(String packetName, String name, PacketListener<T> listener);

    void removePacketListener(String packetName, String name);

    void removePacketListeners(String packetName);

    boolean hasPacketListener(String packetName, String name);

    boolean hasPacketListeners(String packetName);

    Collection<PacketListener<?>> getPacketListeners(String packetName);

    Collection<PacketListener<?>> getPacketListeners(short packetId);
}
