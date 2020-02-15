package com.github.idkp.simplenet.packet;

import java.util.function.Supplier;

public interface PacketPipeOutputConfiguration {
    void registerPacket(short id, String name);

    Short getPacketId(String name);

    void setPacketPayloadEncoder(String packetName, PayloadEncoder<?> encoder);

    PayloadEncoder<?> getPacketPayloadEncoder(String packetName);

    PayloadEncoder<?> getPacketPayloadEncoder(short packetId);

    void setPacketPayloadFactory(String packetName, Supplier<?> factory);

    Supplier<?> getPacketPayloadFactory(String packetName);

    Supplier<?> getPacketPayloadFactory(short packetId);
}
