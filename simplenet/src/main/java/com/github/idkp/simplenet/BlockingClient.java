package com.github.idkp.simplenet;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

public class BlockingClient implements Client {
    private final ClientErrorHandler errorHandler;
    private final PacketHandler packetHandler;
    private Selector selector;
    private ActiveConnection connection;

    public BlockingClient(ClientErrorHandler errorHandler, PacketHandler packetHandler) {
        this.errorHandler = errorHandler;
        this.packetHandler = packetHandler;
    }

    public BlockingClient(PacketHandler packetHandler) {
        this(new ClosingClientErrorHandler(false), packetHandler);
    }

    public BlockingClient() {
        this(new StandardPacketHandler());
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
        connection = new StandardActiveConnection(packetHandler, channel, bufWriter);

        finishListener.run();

        while (true) {
            try {
                selector.select();
            } catch (IOException e) {
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
        try {
            connection.close();
        } finally {
            selector.close();
        }
    }
}
