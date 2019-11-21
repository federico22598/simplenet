package com.github.idkp.simplenet;

import java.nio.ByteBuffer;

public interface PayloadDecoder<T> {
    void decode(ByteBuffer buf);

    T fetchResult();
}
