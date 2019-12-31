package com.github.idkp.simplenet;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class StandardActiveConnection implements ActiveConnection {
    private final SocketChannel channel;
    private final PacketWriter packetWriter;
    private final PacketReader packetReader;

    public StandardActiveConnection(SocketChannel channel,
                                    PacketWriter packetWriter,
                                    PacketReader packetReader) {
        this.channel = channel;
        this.packetWriter = packetWriter;
        this.packetReader = packetReader;
    }

    @Override
    public <T> void sendPacket(String name, T payload) {
        packetWriter.write(name, payload);
    }

    @Override
    public void sendPacket(String name) {
        packetWriter.write(name, null);
    }

    @Override
    public boolean isReadingPacketData() {
        return packetReader.isActive();
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
