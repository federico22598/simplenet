package com.github.idkp.simplenet;

import java.nio.channels.SocketChannel;

public final class FilteredConnectionAttemptResult implements ConnectionAttemptResult {
    private final SocketChannel channel;

    public FilteredConnectionAttemptResult(SocketChannel channel) {
        this.channel = channel;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public Type getType() {
        return Type.FILTERED;
    }
}
