package com.github.idkp.simplenet;

public interface PayloadEncoder<T> {
    void encode(T payload, PacketBufferWriter bufWriter);
}
