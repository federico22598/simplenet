package com.github.idkp.simplenet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class StandardServer extends StandardServerBase {
    private final Map<ClientID, ServerClient> clients = new HashMap<>();
    private final ServerErrorHandler errorHandler;
    private final PipeOpenRequestListener pipeOpenRequestListener;

    private final ClientIDRetriever clientIDRetriever = new ClientIDRetriever();
    private ServerSocketChannel channel;
    private Thread clientAcceptThread;
    private int bindingPort;

    private StandardServer(Builder builder) {
        this.errorHandler = builder.errorHandler;
        this.pipeOpenRequestListener = builder.pipeOpenRequestListener;
    }

    public static StandardServer create(PipeOpenRequestListener pipeOpenRequestListener) {
        return new Builder()
                .withPipeOpenRequestListener(pipeOpenRequestListener)
                .build();
    }

    @Override
    public int getPort() {
        if (!this.isBound()) {
            throw new IllegalStateException("not bound");
        }

        return this.bindingPort;
    }

    @Override
    public boolean bind(int port) throws IOException {
        if (this.isBound()) {
            throw new IllegalStateException("already bound");
        }

        try {
            this.channel = ServerSocketChannel.open();

            this.channel.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            this.channel = null;
            throw e;
        }

        this.bindingPort = port;
        this.clientAcceptThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    accept(this.channel);
                } catch (IOException e) {
                    errorHandler.handle(this, "received", e);
                }
            }
        }, "Server Client Accept Thread");

        this.clientAcceptThread.start();

        return true;
    }

    private void accept(ServerSocketChannel socketChannel) throws IOException {
        SocketChannel pipeChannel = socketChannel.accept();
        ClientID id = clientIDRetriever.retrieve(pipeChannel);
        ServerClient client = clients.computeIfAbsent(
                id, __ -> new StandardServerClient(this, id));

        if (pipeOpenRequestListener != null) {
            pipeOpenRequestListener.received(client, pipeChannel);
        }
    }

    @Override
    public synchronized final boolean isBound() {
        return this.channel != null;
    }

    @Override
    public ServerClient getClient(ClientID id) {
        return clients.get(id);
    }

    @Override
    public void close() throws IOException {
        this.clientAcceptThread.interrupt();

        try {
            this.channel.close();
        } finally {
            this.channel = null;
        }
    }

    @Override
    public String toString() {
        return "StandardServer{bindingPort=" + this.bindingPort + "}";
    }

    @Override
    public boolean equals(Object obj) {
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

    @Override
    protected void unregisterClient(ServerClient client) {
        clients.values().remove(client);
    }

    public static class Builder {
        private ServerErrorHandler errorHandler;
        private PipeOpenRequestListener pipeOpenRequestListener;

        public Builder withErrorHandler(ServerErrorHandler errorHandler) {
            this.errorHandler = errorHandler;

            return this;
        }

        public Builder withPipeOpenRequestListener(PipeOpenRequestListener acceptListener) {
            this.pipeOpenRequestListener = acceptListener;

            return this;
        }

        public StandardServer build() {
            if (pipeOpenRequestListener == null) {
                throw new IllegalStateException("pipeOpenRequestListener == null");
            }

            if (errorHandler == null) {
                errorHandler = new ClosingServerErrorHandler();
            }

            return new StandardServer(this);
        }
    }
}
