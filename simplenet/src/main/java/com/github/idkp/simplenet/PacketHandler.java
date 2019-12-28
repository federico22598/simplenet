package com.github.idkp.simplenet;

import java.io.IOException;
import java.util.function.Supplier;

public interface PacketHandler {
    <T> void sendPacket(String name, T payload, PacketWriter writer);

    void sendPacket(String name, PacketWriter writer);

    ReadResult attemptToReadPacket(PacketReader reader) throws IOException;

    void registerPayloadlessPacket(/*int id, */String name);

    //void registerPacket(int id, String name);

    void setEncoder(String packetName, PayloadEncoder<?> encoder);

    void setDecoder(String packetName, PayloadDecoder<?> decoder);

    void setPayloadFactory(String packetName, Supplier<?> factory);

    <T> boolean addPacketListener(String packetName, String name, PacketListener<T> listener);

    boolean removePacketListener(String packetName, String name);

    boolean removePacketListeners(String packetName);

    boolean hasPacketListener(String packetName, String name);

    boolean hasPacketListeners(String packetName);
}
