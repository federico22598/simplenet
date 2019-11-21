package com.github.idkp.simplenet;

import java.io.IOException;

public final class ClosingServerErrorHandler implements ServerErrorHandler {
    private final boolean silent;

    public ClosingServerErrorHandler(boolean silent) {
        this.silent = silent;
    }

    @Override
    public void handle(String name, Server server) {
        closeQuietly(server);
    }

    @Override
    public void handle(String name, Server server, Exception e) {
        closeQuietly(server);
    }

    private static void closeQuietly(Server server) {
        try {
            server.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handle(String name, Server server, ActiveConnection connection) {
        server.closeConnection(connection);
    }

    @Override
    public void handle(String name, Server server, ActiveConnection connection, Exception e) {
        if (!silent) {
            e.printStackTrace();
        }

        server.closeConnection(connection);
    }
}
