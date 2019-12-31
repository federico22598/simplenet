package com.github.idkp.simplenet;

public final class FilteredConnectionAttemptResult implements ConnectionAttemptResult {

    @Override
    public Type getType() {
        return Type.FILTERED;
    }
}
