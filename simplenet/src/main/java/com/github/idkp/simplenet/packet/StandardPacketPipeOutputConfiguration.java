package com.github.idkp.simplenet.packet;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StandardPacketPipeOutputConfiguration implements PacketPipeOutputConfiguration {
    private final Map<Short, PayloadEncoder<?>> payloadEncoders = new HashMap<>();
    private final Map<String, Short> packetNameToPacketIdMap = new HashMap<>();
    private final Map<Short, Supplier<?>> payloadFactories = new HashMap<>();

    @Override
    public void registerPacket(short id, String name) {
        packetNameToPacketIdMap.putIfAbsent(name, id);
    }

    @Override
    public Short getPacketId(String name) {
        return packetNameToPacketIdMap.get(name);
    }

    @Override
    public void setPacketPayloadEncoder(String packetName, PayloadEncoder<?> encoder) {
        Short id = packetNameToPacketIdMap.get(packetName);

        if (id == null) {
            throw new UnknownPacketException(packetName);
        }

        payloadEncoders.put(id, encoder);
    }

    @Override
    public PayloadEncoder<?> getPacketPayloadEncoder(String packetName) {
        Short id = packetNameToPacketIdMap.get(packetName);

        if (id == null) {
            throw new UnknownPacketException(packetName);
        }

        return payloadEncoders.get(id);
    }

    @Override
    public PayloadEncoder<?> getPacketPayloadEncoder(short packetId) {
        return payloadEncoders.get(packetId);
    }

    @Override
    public void setPacketPayloadFactory(String packetName, Supplier<?> factory) {
        Short id = packetNameToPacketIdMap.get(packetName);

        if (id == null) {
            throw new UnknownPacketException(packetName);
        }

        payloadFactories.put(id, factory);
    }

    @Override
    public Supplier<?> getPacketPayloadFactory(String packetName) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException(packetName);
        }

        return payloadFactories.get(packetId);
    }

    @Override
    public Supplier<?> getPacketPayloadFactory(short packetId) {
        return payloadFactories.get(packetId);
    }
}
