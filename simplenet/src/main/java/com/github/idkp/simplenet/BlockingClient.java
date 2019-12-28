package com.github.idkp.simplenet;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.function.Supplier;

public class BlockingClient implements Client {
    private final ClientErrorHandler errorHandler;
    private final Supplier<PacketHandler> packetHandlerSupplier;
    private Selector selector;
    private ActiveConnection connection;

    public BlockingClient(ClientErrorHandler errorHandler, Supplier<PacketHandler> packetHandlerSupplier) {
        this.errorHandler = errorHandler;
        this.packetHandlerSupplier = packetHandlerSupplier;
    }

    public BlockingClient(Supplier<PacketHandler> packetHandlerSupplier) {
        this(new ClosingClientErrorHandler(false), packetHandlerSupplier);
    }

    public BlockingClient() {
        this(StandardPacketHandler::new);
    }

    @Override
    public void connect(SocketAddress address, Runnable finishListener) throws IOException {
        if (connection != null) {
            throw new AlreadyConnectedException();
        }

        SocketChannel channel = SocketChannel.open(address);

        channel.configureBlocking(false);
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);

        PacketWriter bufWriter = new PacketWriter(channel, selector);
        PacketReader bufReader = new PacketReader(channel);
        PacketHandler packetHandler = packetHandlerSupplier.get();
        connection = new StandardActiveConnection(packetHandler, channel, bufWriter, bufReader);

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
                        bufWriter.writeToChannel();
                    } catch (IOException e) {
                        errorHandler.handle("write", this, e);
                    }
                } else if (key.isReadable()) {
                    try {
                        if (packetHandler.attemptToReadPacket(bufReader) == ReadResult.EOF) {
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
