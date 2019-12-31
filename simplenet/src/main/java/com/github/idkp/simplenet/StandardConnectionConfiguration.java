package com.github.idkp.simplenet;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StandardConnectionConfiguration implements ConnectionConfiguration {
    private final Map<Short, PayloadEncoder<?>> payloadEncoders = new HashMap<>();
    private final Map<Short, PayloadDecoder<?>> payloadDecoders = new HashMap<>();
    private final Map<String, Short> packetNameToPacketIdMap = new HashMap<>();
    private final Map<Short, Map<String, PacketListener<?>>> packetListeners = new HashMap<>();
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
            throw new UnknownPacketException();
        }

        payloadEncoders.put(id, encoder);
    }

    @Override
    public PayloadEncoder<?> getPacketPayloadEncoder(String packetName) {
        Short id = packetNameToPacketIdMap.get(packetName);

        if (id == null) {
            throw new UnknownPacketException();
        }

        return payloadEncoders.get(id);
    }

    @Override
    public PayloadEncoder<?> getPacketPayloadEncoder(short packetId) {
        return payloadEncoders.get(packetId);
    }

    @Override
    public void setPacketPayloadDecoder(String packetName, PayloadDecoder<?> decoder) {
        Short id = packetNameToPacketIdMap.get(packetName);

        if (id == null) {
            throw new UnknownPacketException();
        }

        payloadDecoders.put(id, decoder);
    }

    @Override
    public PayloadDecoder<?> getPacketPayloadDecoder(String packetName) {
        Short id = packetNameToPacketIdMap.get(packetName);

        if (id == null) {
            throw new UnknownPacketException();
        }

        return payloadDecoders.get(id);
    }

    @Override
    public PayloadDecoder<?> getPacketPayloadDecoder(short packetId) {
        return payloadDecoders.get(packetId);
    }

    @Override
    public void setPacketPayloadFactory(String packetName, Supplier<?> factory) {
        Short id = packetNameToPacketIdMap.get(packetName);

        if (id == null) {
            throw new UnknownPacketException();
        }

        payloadFactories.put(id, factory);
    }

    @Override
    public Supplier<?> getPacketPayloadFactory(String packetName) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException();
        }

        return payloadFactories.get(packetId);
    }

    @Override
    public Supplier<?> getPacketPayloadFactory(short packetId) {
        return payloadFactories.get(packetId);
    }

    @Override
    public <T> void addPacketListener(String packetName, String name, PacketListener<T> listener) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException();
        }

        packetListeners.computeIfAbsent(packetId, i -> new HashMap<>())
                .putIfAbsent(name, listener);
    }

    @Override
    public void removePacketListener(String packetName, String name) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException();
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
            throw new UnknownPacketException();
        }

        packetListeners.remove(packetId);
    }

    @Override
    public boolean hasPacketListener(String packetName, String name) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException();
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
            throw new UnknownPacketException();
        }

        return packetListeners.containsKey(packetId);
    }

    @Override
    public Collection<PacketListener<?>> getPacketListeners(String packetName) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException();
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
