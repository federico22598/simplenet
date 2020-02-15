package com.github.idkp.simplenet.packet;

import java.nio.ByteBuffer;

public final class PacketBufferWriter {
    private final PacketWriteData writeData;

    PacketBufferWriter(PacketWriteData writeData) {
        this.writeData = writeData;
    }

    public void write(ByteBuffer buf) {
        writeData.bufQueue.add(new PacketBufWriteData(buf));
    }

    public void writeRepeatedly(ByteBuffer buf, RepeatingBufferWriter writer) {
        writeData.bufQueue.add(new PacketBufWriteData(buf, writer));
    }
}
