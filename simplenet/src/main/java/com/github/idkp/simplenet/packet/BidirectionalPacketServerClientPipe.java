package com.github.idkp.simplenet.packet;

import com.github.idkp.simplenet.ServerClientPipe;
import com.github.idkp.simplenet.StandardServerClient;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class BidirectionalPacketServerClientPipe implements ServerClientPipe {
    private final BidirectionalPacketServer packetServer;
    private SocketChannel clientChannel;
    private RWPipeRegistrationKey pipeRegistrationKey;

    public BidirectionalPacketServerClientPipe(BidirectionalPacketServer packetServer) {
        this.packetServer = packetServer;
    }

    @Override
    public void open(SocketChannel socketChannel, StandardServerClient client) throws IOException {
        clientChannel = socketChannel;
        pipeRegistrationKey = packetServer.registerPipe(socketChannel, client, this);
    }

    @Override
    public void close() throws IOException {
        pipeRegistrationKey.cancel();
        clientChannel.close();
    }

    public PacketWriter getPacketWriter() {
        return pipeRegistrationKey.packetWriter;
    }

    public PacketReader getPacketReader() {
        return pipeRegistrationKey.packetReader;
    }
}
