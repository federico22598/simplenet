package com.github.idkp.simplenet;

import java.io.IOException;

public interface PacketWriter {
    <T> void write(String packetName, T payload);

    void flush() throws IOException;
}
