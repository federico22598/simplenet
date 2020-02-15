package com.github.idkp.simplenet.packet;

import java.nio.channels.SelectionKey;

public final class RWPipeRegistrationKey implements PipeRegistrationKey {
    public final PacketWriter packetWriter;
    public final PacketReader packetReader;
    private final SelectionKey selectionKey;

    RWPipeRegistrationKey(PacketWriter packetWriter,
                          PacketReader packetReader,
                          SelectionKey selectionKey) {
        this.packetWriter = packetWriter;
        this.packetReader = packetReader;
        this.selectionKey = selectionKey;
    }

    @Override
    public void cancel() {
        selectionKey.cancel();
    }
}
