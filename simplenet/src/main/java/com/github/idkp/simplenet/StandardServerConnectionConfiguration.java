package com.github.idkp.simplenet;

import java.net.SocketAddress;

public final class StandardServerConnectionConfiguration extends StandardConnectionConfiguration implements ServerConnectionConfiguration {
    private final SocketAddress address;

    public StandardServerConnectionConfiguration(SocketAddress address) {
        this.address = address;
    }

    @Override
    public SocketAddress getAddress() {
        return address;
    }
}
