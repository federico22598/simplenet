package com.github.idkp.simplenet;

import java.util.ArrayDeque;
import java.util.Queue;

public class PacketWriteData {
    public final short packetId;
    public final Queue<PacketBufWriteData> bufQueue;

    public PacketWriteData(short packetId) {
        this.packetId = packetId;
        this.bufQueue = new ArrayDeque<>();
    }
}