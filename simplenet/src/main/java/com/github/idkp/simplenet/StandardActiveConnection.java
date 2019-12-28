package com.github.idkp.simplenet;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.function.Supplier;

public class StandardActiveConnection implements ActiveConnection {
    private final PacketHandler packetHandler;
    private final SocketChannel channel;
    private final PacketWriter packetWriter;
    private final PacketReader packetReader;

    public StandardActiveConnection(PacketHandler packetHandler,
                                    SocketChannel channel,
                                    PacketWriter packetWriter, PacketReader packetReader) {
        this.packetHandler = packetHandler;
        this.channel = channel;
        this.packetWriter = packetWriter;
        this.packetReader = packetReader;
    }

    @Override
    public <T> void sendPacket(String name, T payload) {
        packetHandler.sendPacket(name, payload, packetWriter);
    }

    @Override
    public void sendPacket(String name) {
        packetHandler.sendPacket(name, packetWriter);
    }

    @Override
    public boolean isReadingPacketData() {
        return packetReader.active();
    }

    @Override
    public void registerPayloadlessPacket(String name) {
        packetHandler.registerPayloadlessPacket(name);
    }

    @Override
    public void setPacketPayloadEncoder(String packetName, PayloadEncoder<?> encoder) {
        packetHandler.setEncoder(packetName, encoder);
    }

    @Override
    public void setPacketPayloadDecoder(String packetName, PayloadDecoder<?> decoder) {
        packetHandler.setDecoder(packetName, decoder);
    }

    @Override
    public void setPacketPayloadFactory(String packetName, Supplier<?> factory) {
        packetHandler.setPayloadFactory(packetName, factory);
    }

    @Override
    public <T> boolean addPacketListener(String packetName, String name, PacketListener<T> listener) {
        return packetHandler.addPacketListener(packetName, name, listener);
    }

    @Override
    public boolean removePacketListener(String packetName, String name) {
        return packetHandler.removePacketListener(packetName, name);
    }

    @Override
    public boolean removePacketListeners(String packetName) {
        return packetHandler.removePacketListeners(packetName);
    }

    @Override
    public boolean hasPacketListener(String packetName, String name) {
        return packetHandler.hasPacketListener(packetName, name);
    }

    @Override
    public boolean hasPacketListeners(String packetName) {
        return packetHandler.hasPacketListeners(packetName);
    }

    @Override
    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
