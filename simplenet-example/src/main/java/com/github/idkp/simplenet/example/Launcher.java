package com.github.idkp.simplenet.example;

import com.github.idkp.simplenet.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class Launcher {
    private static final int SERVER_PORT = 1338;

    public static void main(String[] args) throws IOException {
        startServer();
        startClient();
    }

    private static void startClient() throws IOException {
        Client client = new BlockingClient();
        SocketAddress serverAddress = new InetSocketAddress(InetAddress.getLocalHost(), SERVER_PORT);
        ServerConnectionConfiguration config = new StandardServerConnectionConfiguration(serverAddress);

        config.registerPacket((short) 0, "greetings");
        config.setPacketPayloadEncoder("greetings", new GreetingsPacketPayloadEncoder());
        client.connect(config, () -> {
            ActiveConnection connection = client.getConnection();

            connection.sendPacket("greetings", "Hello, world!");
        });
    }

    private static void startServer() throws IOException {
        Server server = new StandardServer.Builder()
                .withConnectionReviewer(new StandardConnectionReviewer(clientAddr -> {
                    ConnectionConfiguration clientCfg = new StandardConnectionConfiguration();

                    clientCfg.registerPacket((short) 0, "greetings");
                    clientCfg.setPacketPayloadDecoder("greetings", new GreetingsPacketPayloadDecoder());
                    clientCfg.<String>addPacketListener("greetings", "0", System.out::println);
                    return clientCfg;
                }))
                .withConnectionAcceptAttemptHandler(connectionAttemptResult -> {
                    if (connectionAttemptResult.getType() != ConnectionAttemptResult.Type.OK) {
                        return;
                    }

                    ActiveConnection conn = ((OKConnectionAttemptResult) connectionAttemptResult).getConnection();

                    try {
                        System.out.println("Connection established: " + conn.getChannel().getRemoteAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .build();

        server.bind(SERVER_PORT);
    }
}
