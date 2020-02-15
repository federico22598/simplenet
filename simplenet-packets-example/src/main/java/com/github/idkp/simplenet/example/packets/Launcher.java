package com.github.idkp.simplenet.example.packets;

import com.github.idkp.simplenet.*;
import com.github.idkp.simplenet.packet.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public final class Launcher {
    private static final int SERVER_PORT = 1338;

    public static void main(String[] args) throws IOException {
        startServer();
        startClient();
    }

    private static void startClient() throws IOException {
        PacketPipeInputConfiguration packetPipeInputCfg = new StandardPacketPipeInputConfiguration();
        PacketPipeOutputConfiguration packetPipeOutputCfg = new StandardPacketPipeOutputConfiguration();
        BidirectionalPacketClientPipe packetPipe = new BidirectionalPacketClientPipe(new ClosingClientPipeErrorHandler(),
                packetPipeInputCfg, packetPipeOutputCfg, false);

        packetPipeOutputCfg.registerPacket((short) 0, "greetings");
        packetPipeOutputCfg.setPacketPayloadEncoder("greetings", new GreetingsPacketPayloadEncoder());
        packetPipeInputCfg.registerPacket((short) 1, "ack");
        packetPipeInputCfg.addPacketListener("ack", "", __ ->
                packetPipe.getPacketWriter().queuePacket("greetings", "Hello"));

        SocketChannel serverSocketChannel = SocketChannel.open();
        ClientPipeline pipeline = new StandardClientPipeline();

        pipeline.openPipe(packetPipe, serverSocketChannel, new InetSocketAddress(InetAddress.getLocalHost(), SERVER_PORT));
    }

    private static void startServer() throws IOException {
        PacketPipeOutputConfiguration packetPipeOutputCfg = new StandardPacketPipeOutputConfiguration();
        PacketPipeInputConfiguration packetPipeInputCfg = new StandardPacketPipeInputConfiguration();

        packetPipeOutputCfg.registerPacket((short) 1, "ack");
        packetPipeInputCfg.registerPacket((short) 0, "greetings");
        packetPipeInputCfg.setPacketPayloadDecoder("greetings", new GreetingsPacketPayloadDecoder());
        packetPipeInputCfg.addPacketListener("greetings", "", System.out::println);

        BidirectionalPacketServer packetServer = new BidirectionalPacketServer(
                __ -> packetPipeOutputCfg, __ -> packetPipeInputCfg, true);

        Server server = StandardServer.create((client, pipeChannel) -> {
            BidirectionalPacketServerClientPipe packetPipe = new BidirectionalPacketServerClientPipe(packetServer);

            try {
                client.getPipeline().openPipe("packet", packetPipe, pipeChannel);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            packetPipe.getPacketWriter().queuePacket("ack");
        });

        packetServer.open();
        server.bind(SERVER_PORT);
    }
}
