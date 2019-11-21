package com.github.idkp.simplenet;

import java.nio.ByteBuffer;

public final class PacketBufWriteData {
    public final ByteBuffer buf;
    public final RepeatingPacketBufferWriteHandler repeatingBufWriter;

    public PacketBufWriteData(ByteBuffer buf, RepeatingPacketBufferWriteHandler repeatingBufWriter) {
        this.buf = buf;
        this.repeatingBufWriter = repeatingBufWriter;
    }

    public PacketBufWriteData(ByteBuffer buffer) {
        this(buffer, null);
    }
}
