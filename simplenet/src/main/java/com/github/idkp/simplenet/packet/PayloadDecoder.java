package com.github.idkp.simplenet.packet;

import java.nio.ByteBuffer;

public interface PayloadDecoder<T> {
    void decode(ByteBuffer buf);

    T getResult();
}
