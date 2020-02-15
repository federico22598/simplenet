package com.github.idkp.simplenet.packet;

import com.github.idkp.simplenet.ServerClientPipe;
import com.github.idkp.simplenet.StandardServerClient;

final class ClientSelectionKeyData {
    final PacketWriter packetWriter;
    final PacketReader packetReader;
    final StandardServerClient client;
    final ServerClientPipe pipe;

    ClientSelectionKeyData(PacketWriter packetWriter,
                           PacketReader packetReader,
                           StandardServerClient client,
                           ServerClientPipe pipe) {
        this.packetWriter = packetWriter;
        this.packetReader = packetReader;
        this.client = client;
        this.pipe = pipe;
    }
}
