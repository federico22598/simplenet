package com.github.idkp.simplenet;

import java.io.IOException;

public final class PacketReadException extends IOException {
    public PacketReadException() {
        super();
    }

    public PacketReadException(String message) {
        super(message);
    }

    public PacketReadException(Throwable cause) {
        super(cause);
    }
}
