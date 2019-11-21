package com.github.idkp.simplenet.example;

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

        client.connect(serverAddress, () -> {
            ActiveConnection connection = client.getConnection();

            connection.setPacketPayloadEncoder("greetings", new GreetingsPacketPayloadEncoder());
            connection.sendPacket("greetings", "Hello, world!");
        });
    }

    private static void startServer() throws IOException {
        Server server = new StandardServer.Builder()
                .withConnectionReviewer(new StandardConnectionReviewer())
                .withConnectionAcceptAttemptHandler(connectionAttemptResult -> {
                    ActiveConnection conn = connectionAttemptResult.getConnection();

                    conn.setPacketPayloadDecoder("greetings", new GreetingsPacketPayloadDecoder());
                    conn.<String>addPacketReceiveListener("greetings", "", payload -> {
                        System.out.println(payload);

                        try {
                            conn.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                })
                .build();

        server.bind(SERVER_PORT);
    }
}
