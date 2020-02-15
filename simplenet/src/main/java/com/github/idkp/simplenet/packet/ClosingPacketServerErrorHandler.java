package com.github.idkp.simplenet.packet;

import com.github.idkp.simplenet.ServerClientPipe;
import com.github.idkp.simplenet.StandardServerClient;

import java.io.IOException;

public class ClosingPacketServerErrorHandler implements PacketServerErrorHandler {
    private static void close(PacketServer exchangingCentre) {
        try {
            exchangingCentre.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void close(StandardServerClient client, ServerClientPipe pipe) {
        try {
            client.getPipeline().closePipe(pipe);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(PacketServer exchangingCentre, String errorName, StandardServerClient client, ServerClientPipe pipe) {
        close(client, pipe);
    }

    @Override
    public void handle(PacketServer exchangingCentre, String errorName, Exception exception, StandardServerClient client, ServerClientPipe pipe) {
        exception.printStackTrace();
        close(client, pipe);
    }

    @Override
    public void handle(PacketServer exchangingCentre, String errorName) {
        close(exchangingCentre);
    }

    @Override
    public void handle(PacketServer exchangingCentre, String errorName, Exception exception) {
        close(exchangingCentre);
    }
}
