package com.github.idkp.simplenet.packet;

import java.nio.ByteBuffer;

final class PacketBufWriteData {
    final ByteBuffer buf;
    final RepeatingBufferWriter repeatingBufWriter;

    PacketBufWriteData(ByteBuffer buf, RepeatingBufferWriter repeatingBufWriter) {
        this.buf = buf;
        this.repeatingBufWriter = repeatingBufWriter;
    }

    PacketBufWriteData(ByteBuffer buffer) {
        this(buffer, null);
    }
}
