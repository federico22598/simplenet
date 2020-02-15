package com.github.idkp.simplenet.packet;

public final class UnknownPacketException extends RuntimeException {
    public UnknownPacketException(String packetName) {
        super(packetName);
    }
}
