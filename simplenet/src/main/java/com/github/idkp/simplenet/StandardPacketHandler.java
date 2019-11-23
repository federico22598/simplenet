package com.github.idkp.simplenet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class StandardPacketHandler implements PacketHandler {
    private final Map<Short, PayloadEncoder<?>> payloadEncoders = new HashMap<>();
    private final Map<Short, PayloadDecoder<?>> payloadDecoders = new HashMap<>();
    private final Map<String, Short> packetNameToPacketIdMap = new HashMap<>();
    private final Map<Short, Map<String, PacketReceiveListener<Object>>> packetReceiveListeners = new HashMap<>();
    private final Map<Short, Supplier<?>> payloadFactories = new HashMap<>();

    @Override
    public <T> void sendPacket(String name, T payload, PacketWriter writer) {
        Short id = packetNameToPacketIdMap.get(name);

        if (id == null) {
            throw new UnknownPacketException();
        }

        //noinspection unchecked
        PayloadEncoder<T> encoder = (PayloadEncoder<T>) payloadEncoders.get(id);

        if (encoder == null) {
            throw new NoEncoderForPacketException();
        }

        PacketWriteData writeData = new PacketWriteData(id);

        encoder.encode(payload, new StandardPacketBufferWriter(writeData));
        writer.queueWrData(writeData);
        writer.setReadyForChannelWrite();
    }

    @Override
    public void sendPacket(String name, PacketWriter writer) {
        Short id = packetNameToPacketIdMap.get(name);

        if (id == null) {
            sendPayloadlessPacket(name, writer);

            return;
        }

        Supplier<?> payloadFactory = payloadFactories.get(id);

        if (payloadFactory == null) {
            sendPayloadlessPacket(name, writer);

            return;
        }

        PayloadEncoder encoder = payloadEncoders.get(id);

        if (encoder == null) {
            throw new NoEncoderForPacketException();
        }

        Object payload = payloadFactory.get();
        PacketWriteData writeData = new PacketWriteData(id);

        //noinspection unchecked
        encoder.encode(payload, new StandardPacketBufferWriter(writeData));
        writer.queueWrData(writeData);
        writer.setReadyForChannelWrite();
    }

    private void sendPayloadlessPacket(String name, PacketWriter writer) {
        writer.queueWrData(new PacketWriteData(computePacketIdIfAbsent(name)));
        writer.setReadyForChannelWrite();
    }

    @Override
    public ReadResult attemptToReadPacket(PacketReader reader) throws IOException {
        if (reader.ready()) {
            ReadResult headerReadResult = reader.readHeader(payloadDecoders::get);

            if (headerReadResult != ReadResult.COMPLETE) {
                return headerReadResult;
            }
        }

        ReadResult payloadReadResult = reader.readPayload();

        if (payloadReadResult == ReadResult.COMPLETE) {
            Map<String, PacketReceiveListener<Object>> listeners = packetReceiveListeners.get(reader.getPacketId());

            if (listeners != null) {
                for (PacketReceiveListener<Object> listener : listeners.values()) {
                    listener.accept(reader.getPayload());
                }
            }
        }

        return payloadReadResult;
    }

    @Override
    public void registerPayloadlessPacket(String name) {
        computePacketIdIfAbsent(name);
    }

    @Override
    public void setEncoder(String packetName, PayloadEncoder<?> encoder) {
        payloadEncoders.put(computePacketIdIfAbsent(packetName), encoder);
    }

    @Override
    public void setDecoder(String packetName, PayloadDecoder<?> decoder) {
        payloadDecoders.put(computePacketIdIfAbsent(packetName), decoder);
    }

    @Override
    public void setPayloadFactory(String packetName, Supplier<?> factory) {
        payloadFactories.put(computePacketIdIfAbsent(packetName), factory);
    }

    private short computePacketIdIfAbsent(String packetName) {
        return packetNameToPacketIdMap.computeIfAbsent(packetName, p -> (short) packetNameToPacketIdMap.size());
    }

    @Override
    public <T> boolean addPacketReceiveListener(String packetName, String name, PacketReceiveListener<T> listener) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException();
        }

        return packetReceiveListeners.computeIfAbsent(packetId, i -> new HashMap<>()).putIfAbsent(name, (PacketReceiveListener<Object>) listener) == null;
    }

    @Override
    public boolean removePacketReceiveListener(String packetName, String name) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException();
        }

        Map<String, PacketReceiveListener<Object>> listeners = packetReceiveListeners.get(packetId);

        if (listeners == null) {
            return false;
        }

        return listeners.remove(name) != null;
    }

    @Override
    public boolean removePacketReceiveListeners(String packetName) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException();
        }

        return packetReceiveListeners.remove(packetId) != null;
    }

    @Override
    public boolean hasPacketReceiveListener(String packetName, String name) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException();
        }

        Map<String, PacketReceiveListener<Object>> listeners = packetReceiveListeners.get(packetId);

        if (listeners == null) {
            return false;
        }

        return listeners.containsKey(name);
    }

    @Override
    public boolean hasPacketReceiveListeners(String packetName) {
        Short packetId = packetNameToPacketIdMap.get(packetName);

        if (packetId == null) {
            throw new UnknownPacketException();
        }

        return packetReceiveListeners.containsKey(packetId);
    }
}
