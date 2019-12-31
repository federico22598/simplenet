package com.github.idkp.simplenet;

import java.io.IOException;

public interface PacketReader {
    ReadResult read() throws IOException;

    boolean isActive();
}
