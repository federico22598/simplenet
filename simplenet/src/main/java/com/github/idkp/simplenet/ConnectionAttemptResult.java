package com.github.idkp.simplenet;

import java.io.IOException;

public final class ConnectionAttemptResult {
    private final ActiveConnection connection;
    private final IOException iOException;
    private final boolean filtered;

    private ConnectionAttemptResult(ActiveConnection connection, IOException iOException, boolean filtered) {
        this.connection = connection;
        this.iOException = iOException;
        this.filtered = filtered;
    }

    public static ConnectionAttemptResult succeeded(ActiveConnection connection) {
        return new ConnectionAttemptResult(connection, null, false);
    }

    public static ConnectionAttemptResult filtered() {
        return new ConnectionAttemptResult(null, null, true);
    }

    public static ConnectionAttemptResult failed(IOException ioException) {
        return new ConnectionAttemptResult(null, ioException, false);
    }

    public ActiveConnection getConnection() {
        return connection;
    }

    public IOException getIOException() {
        return iOException;
    }

    public boolean wasFiltered() {
        return filtered;
    }

    public boolean hasFailed() {
        return iOException != null;
    }

    public boolean hasSucceeded() {
        return iOException == null && !filtered;
    }
}