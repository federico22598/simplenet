package com.github.idkp.simplenet.packet;

import com.github.idkp.simplenet.ServerClientPipe;
import com.github.idkp.simplenet.StandardServerClient;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class BidirectionalPacketServer implements PacketServer {
    private final PacketPipeOutputConfiguration pipeOutputConfig;
    private final PacketPipeInputConfiguration pipeInputConfig;
    private final PacketServerErrorHandler errorHandler;
    private final boolean openInNewThread;

    private Selector selector;
    private Thread selectorThread;

    public BidirectionalPacketServer(PacketPipeOutputConfiguration pipeOutputConfig,
                                     PacketPipeInputConfiguration pipeInputConfig,
                                     boolean openInNewThread) {
        this(pipeOutputConfig, pipeInputConfig, new ClosingPacketServerErrorHandler(), openInNewThread);
    }

    public BidirectionalPacketServer(PacketPipeOutputConfiguration pipeOutputConfig,
                                     PacketPipeInputConfiguration pipeInputConfig,
                                     PacketServerErrorHandler errorHandler,
                                     boolean openInNewThread) {
        this.pipeOutputConfig = pipeOutputConfig;
        this.pipeInputConfig = pipeInputConfig;
        this.errorHandler = errorHandler;
        this.openInNewThread = openInNewThread;
    }

    @Override
    public void open() throws IOException {
        selector = Selector.open();

        if (openInNewThread) {
            selectorThread = new Thread(this::startSelectorLoop, "Server Packet Exchanging Centre Selector Thread");
            selectorThread.start();
        } else {
            startSelectorLoop();
        }
    }

    private void startSelectorLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (this) {
            }

            try {
                selector.select();
            } catch (IOException e) {
                errorHandler.handle(this, "sel", e);
                return;
            }

            Iterator<SelectionKey> selectedKeysIter = selector.selectedKeys().iterator();

            while (selectedKeysIter.hasNext()) {
                SelectionKey key = selectedKeysIter.next();

                selectedKeysIter.remove();

                if (key.isValid()) {
                    ClientSelectionKeyData data = (ClientSelectionKeyData) key.attachment();

                    if (key.isReadable()) {
                        PacketReader packetReader = data.packetReader;

                        try {
                            if (packetReader.read() == ReadResult.EOF) {
                                errorHandler.handle(this, "eof", data.client, data.pipe);
                            }
                        } catch (IOException e) {
                            errorHandler.handle(this, "retrieve", e, data.client, data.pipe);
                        }
                    } else if (key.isWritable()) {
                        ClientSelectionKeyData keyData = (ClientSelectionKeyData) key.attachment();
                        PacketWriter writer = keyData.packetWriter;

                        try {
                            writer.writeQueuedPackets();
                        } catch (IOException e) {
                            errorHandler.handle(this, "write", e, data.client, data.pipe);
                        }
                    }
                }
            }
        }
    }

    @Override
    public RWPipeRegistrationKey registerPipe(SocketChannel pipeChannel, StandardServerClient client, ServerClientPipe pipe) throws IOException {
        PacketWriter outputWriter = new PacketWriter(pipeChannel, selector, pipeOutputConfig);
        PacketReader inputReader = new PacketReader(pipeChannel, pipeInputConfig);

        pipeChannel.configureBlocking(false);
        SelectionKey channelSelKey;

        synchronized (this) {
            selector.wakeup();
            channelSelKey = pipeChannel.register(selector, SelectionKey.OP_READ, new ClientSelectionKeyData(outputWriter, inputReader, client, pipe));
        }

        return new RWPipeRegistrationKey(outputWriter, inputReader, channelSelKey);
    }

    @Override
    public void close() throws IOException {
        if (selectorThread != null) {
            selectorThread.interrupt();
        }

        selector.close();
    }
}
