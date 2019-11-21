package com.github.idkp.simplenet;

import java.nio.ByteBuffer;

public interface PacketBufferWriter {
    void write(ByteBuffer buf);

    void writeRepeatedly(ByteBuffer buf, RepeatingPacketBufferWriteHandler bufWriter);
}
