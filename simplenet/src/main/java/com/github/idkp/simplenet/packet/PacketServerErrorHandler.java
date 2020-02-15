package com.github.idkp.simplenet.packet;

import com.github.idkp.simplenet.ServerClientPipe;
import com.github.idkp.simplenet.StandardServerClient;

public interface PacketServerErrorHandler {
    void handle(PacketServer exchangingCentre, String errorName, StandardServerClient client, ServerClientPipe pipe);

    void handle(PacketServer exchangingCentre, String errorName, Exception exception, StandardServerClient client, ServerClientPipe pipe);

    void handle(PacketServer exchangingCentre, String errorName);

    void handle(PacketServer exchangingCentre, String errorName, Exception exception);
}
