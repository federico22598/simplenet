package com.github.idkp.simplenet;

import java.nio.channels.SocketChannel;
import java.util.Optional;

public final class FailedConnectionAttemptResult implements ConnectionAttemptResult {
    private final Exception exception;
    private final SocketChannel channel;

    public FailedConnectionAttemptResult(Exception exception, SocketChannel channel) {
        this.exception = exception;
        this.channel = channel;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public Type getType() {
        return Type.FAILED;
    }

    public Optional<SocketChannel> getChannel() {
        return Optional.ofNullable(channel);
    }
}
