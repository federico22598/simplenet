package com.github.idkp.simplenet;

public interface ConnectionAttemptResult {
    Type getType();

    enum Type {
        OK,
        FAILED,
        FILTERED
    }
}
