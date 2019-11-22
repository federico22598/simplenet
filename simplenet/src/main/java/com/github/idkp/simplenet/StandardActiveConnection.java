package com.github.idkp.simplenet;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.function.Supplier;

public final class StandardActiveConnection implements ActiveConnection {
    private final PacketHandler packetHandler;
    private final SocketChannel channel;
    private final Selector selector;
    private final PacketWriter writer;

    public StandardActiveConnection(PacketHandler packetHandler, SocketChannel channel, Selector selector, PacketWriter writer) {
        this.packetHandler = packetHandler;
        this.channel = channel;
        this.selector = selector;
        this.writer = writer;
    }

    @Override
    public <T> void sendPacket(String name, T payload) {
        packetHandler.sendPacket(name, payload, writer);
    }

    @Override
    public void sendPacket(String name) {
        packetHandler.sendPacket(name, writer);
    }

    @Override
    public void makePayloadlessPacketReceivable(String name) {
        packetHandler.makePayloadlessPacketReceivable(name);
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
    public <T> boolean addPacketReceiveListener(String packetName, String name, PacketReceiveListener<T> listener) {
        return packetHandler.addPacketReceiveListener(packetName, name, listener);
    }

    @Override
    public boolean removePacketReceiveListener(String packetName, String name) {
        return packetHandler.removePacketReceiveListener(packetName, name);
    }

    @Override
    public boolean removePacketReceiveListeners(String packetName) {
        return packetHandler.removePacketReceiveListeners(packetName);
    }

    @Override
    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public void close() throws IOException {
        try {
            selector.close();
        } finally {
            channel.close();
        }
    }
}
