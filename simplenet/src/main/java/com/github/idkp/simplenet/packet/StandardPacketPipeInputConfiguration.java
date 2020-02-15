package com.github.idkp.simplenet.packet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StandardPacketPipeInputConfiguration implements PacketPipeInputConfiguration {
    private final Map<Short, PayloadDecoder<?>> payloadDecoders = new HashMap<>();
    private final Map<String, Short> packetNameToPacketIdMap = new HashMap<>();
    private final Map<Short, Map<String, PacketListener<?>>> packetListeners = new HashMap<>();

    @Override
    public void registerPacket(short id, String name) {
        packetNameToPacketIdMap.putIfAbsent(name, id);
    }

    @Override
    public Short getPacketId(String name) {
        return packetNameToPacketIdMap.get(name);
    }

    @Override
    public void setPacketPayloadDecoder(String packetName, PayloadDecoder<?> decoder) {
        Short id = packetNameToPacketIdMap.get(packetName);

        if (id == null) {
            throw new UnknownPacketException(packetName);
        }

        payloadDecoders.put(id, decoder);
    }

    @Override
    public PayloadDecoder<?> getPacketPayloadDecoder(String packetName) {
        Short id = packetNameToPacketIdMap.get(packetName);

        if (id == null) {
            throw new UnknownPacketException(packetName);
        }

        return payloadDecoders.get(id);
    }

    @Override
    public PayloadDecoder<?> getPacketPayloadDecoder(short packetId) {
        return payloadDecoders.get(packetId);
    }

    @Override
    public <T> void addPacketListener(String packetName, String name, PacketListener<T> listener) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException(packetName);
        }

        packetListeners.computeIfAbsent(packetId, i -> new HashMap<>())
                .putIfAbsent(name, listener);
    }

    @Override
    public void removePacketListener(String packetName, String name) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException(packetName);
        }

        Map<String, PacketListener<?>> listeners = packetListeners.get(packetId);

        if (listeners != null) {
            listeners.remove(name);
        }
    }

    @Override
    public void removePacketListeners(String packetName) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException(packetName);
        }

        packetListeners.remove(packetId);
    }

    @Override
    public boolean hasPacketListener(String packetName, String name) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException(packetName);
        }

        Map<String, PacketListener<?>> listeners = packetListeners.get(packetId);

        if (listeners == null) {
            return false;
        }

        return listeners.containsKey(name);
    }

    @Override
    public boolean hasPacketListeners(String packetName) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException(packetName);
        }

        return packetListeners.containsKey(packetId);
    }

    @Override
    public Collection<PacketListener<?>> getPacketListeners(String packetName) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException(packetName);
        }

        Map<String, PacketListener<?>> listeners = packetListeners.get(packetId);

        if (listeners == null) {
            return null;
        }

        return listeners.values();
    }

    @Override
    public Collection<PacketListener<?>> getPacketListeners(short packetId) {
        Map<String, PacketListener<?>> listeners = packetListeners.get(packetId);

        if (listeners == null) {
            return null;
        }

        return listeners.values();
    }
}
