package com.github.idkp.simplenet;

import java.nio.ByteBuffer;

public final class StandardPacketBufferWriter implements PacketBufferWriter {
    private final PacketWriteData writeData;

    public StandardPacketBufferWriter(PacketWriteData writeData) {
        this.writeData = writeData;
    }

    @Override
    public void write(ByteBuffer buf) {
        writeData.bufQueue.add(new PacketBufWriteData(buf));
    }

    @Override
    public void writeRepeatedly(ByteBuffer buf, RepeatingPacketBufferWriteHandler bufWriter) {
        writeData.bufQueue.add(new PacketBufWriteData(buf, bufWriter));
    }
}
