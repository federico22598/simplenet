package com.github.idkp.simplenet.packet;

import java.util.ArrayDeque;
import java.util.Queue;

final class PacketWriteData {
    final short packetId;
    final Queue<PacketBufWriteData> bufQueue;

    PacketWriteData(short packetId) {
        this.packetId = packetId;
        this.bufQueue = new ArrayDeque<>();
    }
}