package com.github.idkp.simplenet.packet;

import com.github.idkp.simplenet.RepeatingBufferWriter;

import java.nio.ByteBuffer;

public final class PacketBufWriteData {
    public final ByteBuffer buf;
    public final RepeatingBufferWriter repeatingBufWriter;

    public PacketBufWriteData(ByteBuffer buf, RepeatingBufferWriter repeatingBufWriter) {
        this.buf = buf;
        this.repeatingBufWriter = repeatingBufWriter;
    }

    public PacketBufWriteData(ByteBuffer buffer) {
        this(buffer, null);
    }
}
