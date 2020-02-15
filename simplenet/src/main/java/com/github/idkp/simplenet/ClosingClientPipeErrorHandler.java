package com.github.idkp.simplenet;

import java.io.IOException;

public class ClosingClientPipeErrorHandler implements ClientPipeErrorHandler {
    private static void closeQuietly(ClientPipe pipe) {
        try {
            pipe.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handle(ClientPipe pipe, String errorName) {
        closeQuietly(pipe);
    }

    @Override
    public void handle(ClientPipe pipe, String errorName, Exception e) {
        e.printStackTrace();
        closeQuietly(pipe);
    }
}
