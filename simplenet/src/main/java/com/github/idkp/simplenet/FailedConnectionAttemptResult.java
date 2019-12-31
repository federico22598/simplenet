package com.github.idkp.simplenet;

public final class FailedConnectionAttemptResult implements ConnectionAttemptResult {
    private final Exception exception;

    public FailedConnectionAttemptResult(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public Type getType() {
        return Type.FAILED;
    }
}
