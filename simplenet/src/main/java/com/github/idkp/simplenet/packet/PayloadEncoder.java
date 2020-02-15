package com.github.idkp.simplenet.packet;

public interface PayloadEncoder<T> {
    void encode(T payload, PacketBufferWriter bufWriter);
}
