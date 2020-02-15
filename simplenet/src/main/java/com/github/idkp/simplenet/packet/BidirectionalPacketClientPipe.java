package com.github.idkp.simplenet.packet;

import com.github.idkp.simplenet.ClientPipe;
import com.github.idkp.simplenet.ClientPipeErrorHandler;
import com.github.idkp.simplenet.ClosingClientPipeErrorHandler;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class BidirectionalPacketClientPipe implements ClientPipe {
    private final ClientPipeErrorHandler errorHandler;
    private final PacketPipeInputConfiguration inputConfig;
    private final PacketPipeOutputConfiguration outputConfig;
    private final boolean useNewThread;

    private Thread selectorThread;
    private SocketChannel socketChannel;
    private Selector selector;
    private PacketWriter packetWriter;
    private PacketReader packetReader;

    public BidirectionalPacketClientPipe(PacketPipeInputConfiguration inputConfig,
                                         PacketPipeOutputConfiguration outputConfig,
                                         boolean useNewThread) {
        this(new ClosingClientPipeErrorHandler(), inputConfig, outputConfig, useNewThread);
    }

    public BidirectionalPacketClientPipe(ClientPipeErrorHandler errorHandler,
                                         PacketPipeInputConfiguration inputConfig,
                                         PacketPipeOutputConfiguration outputConfig,
                                         boolean useNewThread) {
        this.errorHandler = errorHandler;
        this.inputConfig = inputConfig;
        this.outputConfig = outputConfig;
        this.useNewThread = useNewThread;
    }

    @Override
    public void open(SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
        selector = Selector.open();

        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        packetWriter = new PacketWriter(socketChannel, selector, outputConfig);
        packetReader = new PacketReader(socketChannel, inputConfig);

        if (useNewThread) {
            selectorThread = new Thread(() -> startSelecting(packetReader));
            selectorThread.start();
        } else {
            startSelecting(packetReader);
        }
    }

    private void startSelecting(PacketReader packetReader) {
        while (true) {
            try {
                selector.select();
            } catch (IOException | ClosedSelectorException e) {
                errorHandler.handle(this, "sel", e);

                break;
            }

            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();

            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();

                selectedKeys.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isWritable()) {
                    try {
                        packetWriter.writeQueuedPackets();
                    } catch (IOException e) {
                        errorHandler.handle(this, "write", e);
                    }
                } else if (key.isReadable()) {
                    try {
                        if (packetReader.read() == ReadResult.EOF) {
                            errorHandler.handle(this, "eof");
                        }
                    } catch (IOException e) {
                        errorHandler.handle(this, "retrieve", e);
                    }
                }
            }
        }
    }

    public PacketWriter getPacketWriter() {
        return packetWriter;
    }

    public PacketReader getPacketReader() {
        return packetReader;
    }

    @Override
    public void close() throws IOException {
        if (selectorThread != null) {
            selectorThread.interrupt();
        }

        try {
            selector.close();
        } finally {
            socketChannel.close();
        }
    }
}
