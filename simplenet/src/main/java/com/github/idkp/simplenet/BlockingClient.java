package com.github.idkp.simplenet;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;

public class BlockingClient implements Client {
    private final ClientErrorHandler errorHandler;
    private Selector selector;
    private ActiveConnection connection;

    public BlockingClient(ClientErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public BlockingClient() {
        this(new ClosingClientErrorHandler(false));
    }

    @Override
    public void connect(ServerConnectionConfiguration configuration, Runnable finishListener) throws IOException {
        if (connection != null) {
            throw new AlreadyConnectedException();
        }

        SocketChannel channel = SocketChannel.open(configuration.getAddress());
        selector = Selector.open();

        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);

        PacketWriter writer = new StandardPacketWriter(channel, selector, configuration);
        PacketReader reader = new StandardPacketReader(channel, configuration);
        connection = new StandardActiveConnection(channel, writer, reader, configuration);

        finishListener.run();

        while (true) {
            try {
                selector.select();
            } catch (IOException | ClosedSelectorException e) {
                errorHandler.handle("sel", this, e);

                break;
            }

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isWritable()) {
                    try {
                        writer.flush();
                    } catch (IOException e) {
                        errorHandler.handle("write", this, e);
                    }
                } else if (key.isReadable()) {
                    try {
                        if (reader.read() == ReadResult.EOF) {
                            errorHandler.handle("eof", this);
                        }
                    } catch (IOException e) {
                        errorHandler.handle("read", this, e);
                    }
                }

                selectedKeys.remove();
            }
        }
    }

    @Override
    public ActiveConnection getConnection() {
        if (connection == null) {
            throw new NotYetConnectedException();
        }

        return connection;
    }

    @Override
    public boolean isConnected() {
        return connection != null;
    }

    @Override
    public void close() throws IOException {
        if (!isConnected()) {
            return;
        }

        try {
            connection.close();
        } finally {
            try {
                selector.close();
            } finally {
                connection = null;
            }
        }
    }
}
