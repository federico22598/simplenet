package com.github.idkp.simplenet.packet;

public interface PacketListener<T> {
    void received(T payload);
}
