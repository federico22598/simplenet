package com.github.idkp.simplenet;

import java.io.IOException;

public class ClosingServerErrorHandler implements ServerErrorHandler {

    @Override
    public void handle(Server server, String errorName, Exception e) {
        e.printStackTrace();
        closeQuietly(server);
    }

    private static void closeQuietly(Server server) {
        try {
            server.close();
        } catch (IOException ignored) {
        }
    }
}
