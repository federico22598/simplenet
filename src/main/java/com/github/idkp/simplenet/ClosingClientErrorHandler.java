package com.github.idkp.simplenet;

import java.io.IOException;

public final class ClosingClientErrorHandler implements ClientErrorHandler {
    private final boolean silent;

    public ClosingClientErrorHandler(boolean silent) {
        this.silent = silent;
    }

    @Override
    public void handle(String name, Client client) {
        closeQuietly(client);
    }

    @Override
    public void handle(String name, Client client, Exception e) {
        if (!silent) {
            e.printStackTrace();
        }

        closeQuietly(client);
    }

    private static void closeQuietly(Client client) {
        try {
            client.getConnection().close();
        } catch (IOException ignored) {
        }
    }
}
