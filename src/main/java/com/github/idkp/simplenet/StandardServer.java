package com.github.idkp.simplenet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StandardServer implements Server {
    private static final Logger LOGGER = Logger.getLogger(StandardServer.class.getName());

    private final ConnectionReviewer connectionReviewer;
    private final Map<SocketAddress, ActiveConnection> activeConnections = new HashMap<>();
    private final ServerErrorHandler errorHandler;
    private final Consumer<ConnectionAttemptResult> connectionAcceptAttemptHandler;

    private Selector selector;
    private ServerSocketChannel channel;
    private Thread selectorSelectThread;
    private int bindingPort;

    private StandardServer(Builder builder) {
        this.connectionReviewer = builder.connectionReviewer;
        this.errorHandler = builder.errorHandler;
        this.connectionAcceptAttemptHandler = builder.connectionAcceptAttemptHandler;
    }

    public static StandardServer create(ConnectionReviewer connectionReviewer) {
        return new Builder()
                .withConnectionReviewer(connectionReviewer)
                .build();
    }

    @Override
    public int getPort() {
        if (!this.isBound()) {
            throw new IllegalStateException("Server is not bound.");
        }

        return this.bindingPort;
    }

    @Override
    public ConnectionReviewer getConnectionReviewer() {
        return connectionReviewer;
    }

    @Override
    public boolean bind(int port) throws IOException {
        if (this.isBound()) {
            throw new IllegalStateException("Server already bound.");
        }

        try {
            this.channel = ServerSocketChannel.open();

            this.channel.configureBlocking(false);

            this.selector = Selector.open();

            this.channel.bind(new InetSocketAddress(port));
            this.channel.register(this.selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            this.channel = null;

            throw e;
        }

        this.bindingPort = port;

        this.selectorSelectThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && this.isBound()) {
                try {
                    selector.select();
                } catch (IOException e) {
                    errorHandler.handle("sel", this, e);

                    return;
                }

                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();

                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        processAcceptableKey();
                    } else if (key.isWritable()) {
                        processWritableKey(key);
                    } else if (key.isReadable()) {
                        processReadableKey(key);
                    }
                }
            }
        }, "Server Client Accept Thread");

        this.selectorSelectThread.start();

        LOGGER.log(Level.INFO, "{0} initialized.", this.toString());

        return true;
    }

    private void processAcceptableKey() {
        ConnectionAttemptResult result = this.connectionReviewer.accept(this);
        IOException iOException = result.getIOException();

        if (iOException == null) {
            ActiveConnection connection = result.getConnection();

            if (connection != null) {
                try {
                    activeConnections.put(
                            connection.getChannel().getRemoteAddress(),
                            connection);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            errorHandler.handle("accept", this, iOException);
        }

        if (connectionAcceptAttemptHandler != null) {
            connectionAcceptAttemptHandler.accept(result);
        }
    }

    private void processWritableKey(SelectionKey key) {
        ServerSelectorKeyData keyData = (ServerSelectorKeyData) key.attachment();
        PacketWriter writer = keyData.packetWriter;

        try {
            writer.writeToChannel();
        } catch (IOException e) {
            errorHandler.handle("write", this, keyData.conn, e);
        }
    }

    private void processReadableKey(SelectionKey key) {
        ServerSelectorKeyData keyData = (ServerSelectorKeyData) key.attachment();
        PacketHandler packetHandler = keyData.packetHandler;

        try {
            if (packetHandler.attemptToReadPacket(keyData.packetReader) == ReadResult.EOF) {
                errorHandler.handle("eof", this, keyData.conn);
            }
        } catch (IOException e) {
            errorHandler.handle("read", this, keyData.conn, e);
        }
    }

    @Override
    public void closeConnection(ActiveConnection connection) {
        SelectionKey key = connection.getChannel().keyFor(selector);

        if (key != null) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            key.cancel();
            activeConnections.values().remove(connection);
        }
    }

    @Override
    public Selector getSelector() {
        return selector;
    }

    @Override
    public ServerSocketChannel getChannel() {
        return channel;
    }

    @Override
    public void unbind() throws IOException {
        if (this.channel == null) {
            return;
        }

        try {
            this.channel.close();
        } finally {
            this.channel = null;
        }
    }

    @Override
    public synchronized boolean isBound() {
        return this.channel != null;
    }

    @Override
    public ActiveConnection getConnection(SocketAddress address) {
        return activeConnections.get(address);
    }

    @Override
    public void close() throws IOException {
        if (this.selectorSelectThread != null) {
            this.selectorSelectThread.interrupt();
            this.selectorSelectThread = null;
        }

        this.unbind();

        LOGGER.log(Level.INFO, "Server {0} closed.", this.toString());
    }

    @Override
    public String toString() {
        return "StandardServer{bindingPort=" + this.bindingPort + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Server)) {
            return false;
        }

        Server otherServer = (Server) obj;

        return otherServer.getPort() == this.bindingPort;
    }

    @Override
    public int hashCode() {
        return 31 + this.bindingPort;
    }

    public static class Builder {
        private ConnectionReviewer connectionReviewer;
        private ServerErrorHandler errorHandler;
        private Consumer<ConnectionAttemptResult> connectionAcceptAttemptHandler;

        public Builder withConnectionReviewer(ConnectionReviewer connectionReviewer) {
            this.connectionReviewer = connectionReviewer;

            return this;
        }

        public Builder withErrorHandler(ServerErrorHandler errorHandler) {
            this.errorHandler = errorHandler;

            return this;
        }

        public Builder withConnectionAcceptAttemptHandler(Consumer<ConnectionAttemptResult> connectionAcceptAttemptHandler) {
            this.connectionAcceptAttemptHandler = connectionAcceptAttemptHandler;

            return this;
        }

        public StandardServer build() {
            if (connectionReviewer == null) {
                throw new IllegalStateException("connectionReviewer == null");
            }

            if (errorHandler == null) {
                errorHandler = new ClosingServerErrorHandler(false);
            }

            return new StandardServer(this);
        }
    }
}
